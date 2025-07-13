package com.google.ar.sceneform.samples.gltf.library.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.MiniGameDao
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.PointsDao
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.BrainPointsEntity
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.MiniGameEntity
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [ModelEntity::class, BrainPointsEntity::class, MiniGameEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun modelDao(): ModelDao
    abstract fun brainPointsDao(): PointsDao // Fix: Ensure consistency with ViewModel
    abstract fun miniGameDao(): MiniGameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "model_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope)) // Add database callback here
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Callback to populate database on creation
    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    try {
                        val modelDao = database.modelDao()
                        val brainPointsDao = database.brainPointsDao()
                        val miniGameDao = database.miniGameDao()

                        val existingGames = miniGameDao.getAllMiniGames()
                        Log.d("DatabaseDebug", "Existing games on create: $existingGames")

                        if (existingGames.isEmpty()) {
                            populateDatabase(modelDao, brainPointsDao, miniGameDao)
                        }
                    } catch (e: Exception) {
                        Log.e("DatabaseDebug", "Error inserting initial data: ${e.message}")
                    }
                }
            }
        }

        //populate the database with models
        suspend fun populateDatabase(
            modelDao: ModelDao,
            brainPointsDao: PointsDao,
            miniGameDao: MiniGameDao
        ) {
            //{FIRST BATCH OF 1-9 PAGES}
            //page 223 (num_1)
            modelDao.insertModel(ModelEntity("Amphibian", "models/amphibian.glb", R.layout.amphibian_infos, "Tap to learn more!", R.raw.froggy))
            modelDao.insertModel(ModelEntity("Bacteria", "models/bacteria.glb", R.layout.bacteria_infos, "Tap to explore bacterial structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Digestive", "models/digestive.glb", R.layout.digestive_infos, "Tap to see the digestive process!", R.raw.digestsound))
            modelDao.insertModel(ModelEntity("Platypus", "models/platypus.glb", R.layout.platypus_infos, "Tap to discover platypus facts!", R.raw.platypusound))
            modelDao.insertModel(ModelEntity("Heart", "models/heart.glb", R.layout.heart_info, "Tap to see the heart in action!", R.raw.heartsound))
            modelDao.insertModel(ModelEntity("Microscope", "models/microscope.glb", R.layout.microscope_infos, "Tap to see the microscope in action!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Earth", "models/earth.glb", R.layout.earth_infos, "Tap to see the earth in action!", R.raw.sonarping))
            modelDao.insertModel(ModelEntity("Animal", "models/animal.glb", R.layout.animal_infos, "Tap to see the animals in action!", R.raw.animals_sound))
            modelDao.insertModel(ModelEntity("Plant", "models/plant.glb", R.layout.plant_info, "Tap to see the plants in action!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Biotic", "models/biotic.glb", R.layout.biotic_infos, "Tap to see the biotic in action!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Cell", "models/cell.glb", R.layout.cell_info, "Tap to see the cell in action!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Biodiversity", "models/biodiversity.glb", R.layout.biodiversity_info, "Tap to see the biodiversity in action!", R.raw.animals_sound, R.raw.biodiversity))
            modelDao.insertModel(ModelEntity("Organism", "models/organism.glb", R.layout.organism_info, "Tap to see the organism in action!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Ecosystem", "models/ecosystem.glb", R.layout.ecosystem_info, "Tap to see the ecosystem in action!", R.raw.animals_sound))
            modelDao.insertModel(ModelEntity("Agriculture", "models/agriculture.glb", R.layout.agriculture_info, "Tap to see the agriculture in action!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Medicine", "models/medicine.glb", R.layout.medicine_info, "Tap to see the medicine in action!", R.raw.popup))

            //page 224 (num_2)
            modelDao.insertModel(ModelEntity("Reptile", "models/reptile.glb", R.layout.reptile_info, "Tap to see the reptile in action!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Forest", "models/forest.glb", R.layout.forest_info, "Tap to see the forest in action!", R.raw.slender))
            modelDao.insertModel(ModelEntity("Ocean", "models/ocean.glb", R.layout.ocean_info, "Tap to see the ocean in action!", R.raw.ocean))

            modelDao.insertModel(ModelEntity("River", "models/river.glb", R.layout.river_info, "Tap to see the river in action!", R.raw.ocean))

            modelDao.insertModel(ModelEntity("Soil", "models/soil.glb", R.layout.soil_info, "Tap to see the soil in action!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Human", "models/human.glb", R.layout.human_info, "Tap to see the human in action!", R.raw.human))
            modelDao.insertModel(ModelEntity("Acacia", "models/acacia.glb", R.layout.acacia_info, "Tap to see the acacia in action!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Bacterium", "models/bacteria.glb", R.layout.bacteria_infos, "Tap to explore bacterial structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Biological", "models/biological.glb", R.layout.biological_info, "Tap to explore biological structures!", R.raw.animals_sound))
            modelDao.insertModel(ModelEntity("Specie", "models/specie.glb", R.layout.specie_info, "Tap to explore specie structures!", R.raw.popup))

            //page 225 (num_3)
            // [organism] (already implemented)

            //page 226 (num_4)
            modelDao.insertModel(ModelEntity("Scientist", "models/scientist.glb", R.layout.scientist_info, "Tap to explore scientist structures!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Classification", "models/classification.glb", R.layout.classification_info, "Tap to explore classification structures!", R.raw.popup, R.raw.classification))
            modelDao.insertModel(ModelEntity("Phyla", "models/classification.glb", R.layout.classification_info, "Tap to explore Phyla structures!", R.raw.popup, R.raw.classification))
            modelDao.insertModel(ModelEntity("Fertile", "models/fertile.glb", R.layout.fertile_offspring, "Tap to explore fertile_offspring structures!", R.raw.popup, R.raw.fertileoffspring))
            modelDao.insertModel(ModelEntity("Domain", "models/domain.glb", R.layout.three_domains_of_life, "Tap to explore three domain structures!", R.raw.popup, R.raw.threedomainsoflife))
            modelDao.insertModel(ModelEntity("Yote", "models/yote.glb", R.layout.yote_info, "Tap to explore yote structures!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Nucleus", "models/nucleus.glb", R.layout.nucleus_info, "Tap to explore nucleus structures!", R.raw.popup, R.raw.nucleusmeaning))
            modelDao.insertModel(ModelEntity("Chromosome", "models/chromosome.glb", R.layout.chromosome_info, "Tap to explore chromosome structures!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Dna", "models/dna.glb", R.layout.dna_info, "Tap to explore dna structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Heredity", "models/heredity.glb", R.layout.heredity_info, "Tap to explore heredity structures!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Membrane", "models/membrane.glb", R.layout.membrane_info, "Tap to explore membrane structures!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Cellular", "models/cellular.glb", R.layout.cellular_info, "Tap to explore cellular structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Microorganism", "models/microorganism.glb", R.layout.microorganism_info, "Tap to explore microorganism structures!", R.raw.bacteriasound))

            //page 228 (num_6)
            modelDao.insertModel(ModelEntity("Methane", "models/methane.glb", R.layout.methane_info, "Tap to explore methane structures!", R.raw.fart))

            //page 229 (num_7)
            modelDao.insertModel(ModelEntity("Hydrogen", "models/hydrogen.glb", R.layout.hydrogen_info, "Tap to explore hydrogen structures!", R.raw.popup))
            modelDao.insertModel(ModelEntity("Salinarum", "models/salinarum.glb", R.layout.salinarum_info, "Tap to salinarum hydrogen structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Halococcus", "models/halococcus.glb", R.layout.halococcus_info, "Tap to halococcus hydrogen structures!", R.raw.bacteriasound))


            //-------- 10 Pages [223 - 242] -------------
            // Page 233 (6)
            modelDao.insertModel(ModelEntity("Cyano", "models/cyano.glb", R.layout.cyano_info, "Tap to cyano cyano structures!", R.raw.bacteriasound))

            // Page 234 (6)
            modelDao.insertModel(ModelEntity("Protist", "models/protist.glb", R.layout.protist_info, "Tap to protist protist structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Chlorophyll", "models/chlorophyll.glb", R.layout.chlorophyll_info, "Tap to protist chlorophyll structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Dinoflagellate", "models/dinoflagellate.glb", R.layout.dinoflagellate_info, "Tap to protist dinoflagellate structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Euglanoid", "models/euglanoid.glb", R.layout.euglanoid_info, "Tap to protist euglanoid structures!", R.raw.bacteriasound))

            // Page 235 (7)
            modelDao.insertModel(ModelEntity("Chlorella", "models/chlorella.glb", R.layout.chlorella_info, "Tap to protist chlorella structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Pediastrum", "models/pediastrum.glb", R.layout.pediastrum_info, "Tap to protist pediastrum structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Spirogyra", "models/spirogyra.glb", R.layout.spirogyra_info, "Tap to protist spirogyra structures!", R.raw.bacteriasound))

            // Page 237 (8)
            modelDao.insertModel(ModelEntity("Eucheuma", "models/eucheuma.glb", R.layout.eucheuma_info, "Tap to protist eucheuma structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Gracilaria", "models/gracilaria.glb", R.layout.gracilaria_info, "Tap to  gracilaria structures!", R.raw.bacteriasound))

            // Page 238 (8)
            modelDao.insertModel(ModelEntity("Radiolarian", "models/radiolarian.glb", R.layout.radiolarian_info, "Tap to  radiolarian structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Foraminifera", "models/foraminifera.glb", R.layout.foraminifera_info, "Tap to foraminifera structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Amoeba", "models/amoeba.glb", R.layout.amoeba_info, "Tap to protist amoeba structures!", R.raw.bacteriasound))

            // Page 239 (9)
            modelDao.insertModel(ModelEntity("Paramecium", "models/paramecium.glb", R.layout.paramecium_info, "Tap to protist paramecium structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Didinium", "models/didinium.glb", R.layout.didinium_info, "Tap to protist didinium structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Vorticella", "models/vorticella.glb", R.layout.vorticella_info, "Tap to protist vorticella structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Slime", "models/slime.glb", R.layout.slime_info, "Tap to protist slime structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Watermold", "models/watermold.glb", R.layout.watermold_info, "Tap to protist watermold structures!", R.raw.bacteriasound))

            // Page 240 (9)
            modelDao.insertModel(ModelEntity("Saprophyte", "models/saprophyte.glb", R.layout.saprophyte_info, "Tap to  saprophyte structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Hyphae", "models/hyphae.glb", R.layout.hyphae_info, "Tap to  hyphae structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Rhizoid", "models/rhizoid.glb", R.layout.rhizoid_info, "Tap to rhizoid structures!", R.raw.bacteriasound))

            // Page 242 (10)
            modelDao.insertModel(ModelEntity("Vascular", "models/vascular.glb", R.layout.vascular_info, "Tap to vascular structures!", R.raw.bacteriasound))

            // Page 242 (10)
            modelDao.insertModel(ModelEntity("Peat", "models/peat.glb", R.layout.peat_info, "Tap to protist peat structures!", R.raw.bacteriasound))

            //10 Pages [243- 262]
            // Page 244 (1)
            modelDao.insertModel(ModelEntity("Sperms", "models/sperms.glb", R.layout.sperms_info, "Tap to protist sperm structures!", R.raw.bacteriasound, R.raw.angioandgymno))

            // Page 246 (2)
            modelDao.insertModel(ModelEntity("Cot", "models/cot.glb", R.layout.cot_info, "Tap to cot structures!", R.raw.bacteriasound, R.raw.monocotanddicot))
            modelDao.insertModel(ModelEntity("Leaf venation", "models/leaf venation.glb", R.layout.leafvenation_info, "Tap to leaf venation structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Stem", "models/stem.glb", R.layout.stem_info, "Tap to stem structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Flower plan", "models/flower plan.glb", R.layout.flower_plan_info, "Tap to flower plan structures!", R.raw.bacteriasound))

            // Page 247 (3)
            modelDao.insertModel(ModelEntity("Harmful plant", "models/harmful plant.glb", R.layout.harmful_plant_info, "Tap to harmful plant structures!", R.raw.bacteriasound, R.raw.harmfulplants))

            // Page 249 (4)
            modelDao.insertModel(ModelEntity("Sponge", "models/sponge.glb", R.layout.sponge_info, "Tap to sponge structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Spiculis", "models/spiculis.glb", R.layout.spiculis_info, "Tap to spiculis structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Osculum", "models/osculum.glb", R.layout.osculum_info, "Tap to osculum structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Cnidarian", "models/cnidarian.glb", R.layout.cdinarian_info, "Tap to cnidarian structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Nematocyst", "models/nematocyst.glb", R.layout.nematocyst_info, "Tap to nematocyst structures!", R.raw.bacteriasound))

            // Page 250 (4)
            modelDao.insertModel(ModelEntity("Flatworm", "models/flatworm.glb", R.layout.flatworm_info, "Tap to flatworm structures!", R.raw.bacteriasound))

            // Page 251 (5)
            modelDao.insertModel(ModelEntity("Roundworm", "models/roundworm.glb", R.layout.roundworm_info, "Tap to roundworm structures!", R.raw.bacteriasound))

            // Page 252 (5)
            modelDao.insertModel(ModelEntity("Segmentedworm", "models/segmentedworm.glb", R.layout.segmentedworm_info, "Tap to segmentedworm structures!", R.raw.bacteriasound))

            // Page 254 (6)
            modelDao.insertModel(ModelEntity("Echinoderm", "models/echinoderm.glb", R.layout.echinoderm_info, "Tap to Echinoderm structures!", R.raw.bacteriasound))

            // Page 255 (7)
            modelDao.insertModel(ModelEntity("Arthropod", "models/arthropod.glb", R.layout.arthropod_info, "Tap to arthropod structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Exoskeleton", "models/exoskeleton.glb", R.layout.exoskeleton_info, "Tap to exoskeleton structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Crustacean", "models/crustacean.glb", R.layout.crustacean_info, "Tap to crustacean structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Arachnid", "models/arachnid.glb", R.layout.arachnid_info, "Tap to arachnid structures!", R.raw.bacteriasound))

            // Page 256 (7)
            modelDao.insertModel(ModelEntity("Pede", "models/pede.glb", R.layout.pede_info, "Tap to pede structures!", R.raw.bacteriasound))

            // Page 258 (8)
            modelDao.insertModel(ModelEntity("Chordate", "models/chordate.glb", R.layout.chordate_info, "Tap to chordate structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Turnicate", "models/turnicate.glb", R.layout.turnicate_info, "Tap to turnicate structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Vertebrate", "models/vertebrate.glb", R.layout.vertebrate_info, "Tap to vertebrate structures!", R.raw.bacteriasound))

            //10 Pages [263- 282]
            // Page 264 (1)
            modelDao.insertModel(ModelEntity("Eutherians", "models/eutherians.glb", R.layout.eutherians_info, "Tap to eutherians structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Mammal", "models/mammal.glb", R.layout.mammal_info, "Tap to mammal structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Marsupials", "models/marsupials.glb", R.layout.marsupials_info, "Tap to marsupials structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Monotremes", "models/monotremes.glb", R.layout.monotremes_info, "Tap to monotremes structures!", R.raw.bacteriasound))

            //--------------[Second Batch] Pages [283- 342]
            //10 Pages [288- 302]
            // Page 264 (1)
            modelDao.insertModel(ModelEntity("Monoculture", "models/monoculture.glb", R.layout.monoculture_info, "Tap to monoculture structures!", R.raw.bacteriasound))

            // Page 291 (5)
            modelDao.insertModel(ModelEntity("Digestion", "models/digestion.glb", R.layout.digestion_info, "Tap to digestion structures!", R.raw.bacteriasound))

            // Page 300 (9)
            modelDao.insertModel(ModelEntity("Bromelain", "models/bromelain.glb", R.layout.bromelain_info, "Tap to bromelain structures!", R.raw.bacteriasound))

            //10 Pages [303- 322]
            // Page 303 (1)
            modelDao.insertModel(ModelEntity("Carbohydrate", "models/carbohydrate.glb", R.layout.carbohydrate_info, "Tap to carbohydrate structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Lipid", "models/lipid.glb", R.layout.lipid_info, "Tap to lipid structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Protein", "models/protein.glb", R.layout.protein_info, "Tap to protein structures!", R.raw.bacteriasound))

            // Page 303 (5)
            modelDao.insertModel(ModelEntity("Fiber", "models/fiber.glb", R.layout.fiber_info, "Tap to fiber structures!", R.raw.bacteriasound))

            //10 Pages [323- 342]
            // Page 327 (3)
            modelDao.insertModel(ModelEntity("Spermato", "models/spermato.glb", R.layout.spermato_info, "Tap to spermato structures!", R.raw.bacteriasound))

            // Page 328 (3)
            modelDao.insertModel(ModelEntity("Down syndrome", "models/down syndrome.glb", R.layout.down_syndrome_info, "Tap to down syndrome structures!", R.raw.bacteriasound))

            // Page 329 (4)
            modelDao.insertModel(ModelEntity("Cri du chat", "models/cri du chat.glb", R.layout.cri_du_chat_info, "Tap to Cri du chat structures!", R.raw.bacteriasound))

            // Page 327 (4)
            modelDao.insertModel(ModelEntity("First filial generation", "models/first filial generation.glb", R.layout.firstfilialgeneration_info, "Tap to first filial generationstructures!", R.raw.bacteriasound, R.raw.monocotanddicot))


            // Ensure Initial Brain Points Exist
            brainPointsDao.updatePoints(0)

            // Populate the database with contents ready to be unlocked and interacted as rewards
 /*           miniGameDao.insertGame(MiniGameEntity(gameId = "blueGuy", name = "Blue Guy", isUnlocked = false, isInstalled = false))
            miniGameDao.insertGame(MiniGameEntity(gameId = "breakBaller", name = "Break Baller", isUnlocked = false, isInstalled = false))
            miniGameDao.insertGame(MiniGameEntity(gameId = "snakeGame", name = "Snake Game", isUnlocked = false, isInstalled = false))
*/
            // Check and insert Blue Guy
            if (miniGameDao.getMiniGameById("blueGuy") == null) {
                miniGameDao.insertGame(
                    MiniGameEntity(
                        gameId = "blueGuy",
                        name = "Blue Guy",
                        isUnlocked = false,
                        isInstalled = false
                    )
                )
            }

            // Check and insert Break Baller
            if (miniGameDao.getMiniGameById("breakBaller") == null) {
                miniGameDao.insertGame(
                    MiniGameEntity(
                        gameId = "breakBaller",
                        name = "Break Baller",
                        isUnlocked = false,
                        isInstalled = false
                    )
                )
            }

            // Check and insert Snake Game
            if (miniGameDao.getMiniGameById("snakeGame") == null) {
                miniGameDao.insertGame(
                    MiniGameEntity(
                        gameId = "snakeGame",
                        name = "Snake Game",
                        isUnlocked = false,
                        isInstalled = false
                    )
                )
            }

            // Check and insert rageSailor
            if (miniGameDao.getMiniGameById("rageSailor") == null) {
                miniGameDao.insertGame(
                    MiniGameEntity(
                        gameId = "rageSailor",
                        name = "Rage Sailor",
                        isUnlocked = false,
                        isInstalled = false
                    )
                )
            }

            // Check and insert flyingBlock
            if (miniGameDao.getMiniGameById("flyingBlock") == null) {
                miniGameDao.insertGame(
                    MiniGameEntity(
                        gameId = "flyingBlock",
                        name = "Flying Block",
                        isUnlocked = false,
                        isInstalled = false
                    )
                )
            }
            Log.d("DatabaseDebug", "Inserted initial rewards")



        }



/*        suspend fun populateDatabase(miniGameDao: MiniGameDao) {
            Log.d("DatabaseDebug", "Inserting initial mini-games")
            miniGameDao.insertGame(MiniGameEntity("1", "RewardsContent1", false))
            miniGameDao.insertGame(MiniGameEntity("2", "RewardsContent2", false))
            miniGameDao.insertGame(MiniGameEntity("11", "RewardsContent3", false))

            Log.d("DatabaseDebug", "Inserted mini-games successfully!")
        }*/
    }


    }

