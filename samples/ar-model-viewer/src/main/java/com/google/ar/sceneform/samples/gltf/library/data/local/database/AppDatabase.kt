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
    version = 3,
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


        suspend fun populateDatabase(
            modelDao: ModelDao,
            brainPointsDao: PointsDao,
            miniGameDao: MiniGameDao
        ) {
            //populate the database with models
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
            // Ensure Initial Brain Points Exist
            brainPointsDao.updatePoints(0)
 
            // Populate the database with contents ready to be unlocked and interacted as rewards
            miniGameDao.insertGame(MiniGameEntity("1", "Reward Item 1", false))
            miniGameDao.insertGame(MiniGameEntity("2", "Reward Item 2", false))
            miniGameDao.insertGame(MiniGameEntity("3", "Reward Item 3", false))
            miniGameDao.insertGame(MiniGameEntity("4", "Reward Item 4", false))
            miniGameDao.insertGame(MiniGameEntity("5", "Unity Mini-Game", false)) // Unity Mini-Game to be unlocked


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

