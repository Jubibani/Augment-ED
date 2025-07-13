package com.google.ar.sceneform.samples.gltf.library.screens

import com.google.ar.sceneform.samples.gltf.library.fragments.LibraryFragment
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.helpers.VideoPlayerHelper
import com.google.ar.sceneform.samples.gltf.library.theme.AugmentEDTheme


class LibraryActivity : FragmentActivity() {

    private lateinit var backSound: MediaPlayer
    private lateinit var flipSound: MediaPlayer
    private lateinit var switchSound: MediaPlayer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backSound = MediaPlayer.create(this, R.raw.back)
        flipSound = MediaPlayer.create(this, R.raw.flip)
        switchSound = MediaPlayer.create(this, R.raw.swipe)


        setContent {

            AugmentEDTheme {

                LibraryScreen(
                    finish = { finish() },
                    playBackSound = { playBackSound() },
                    playFlipSound = { playFlipSound() },
                    playSwitchSound = { playSwitchSound() },
                    onModelSelected = { modelData ->
                        val libraryFragment = LibraryFragment().apply {
                            arguments = Bundle().apply {
                                putString("modelName", modelData.name) // modelData is now correctly defined
                            }
                        }

                        supportFragmentManager.beginTransaction()
                            .replace(android.R.id.content, libraryFragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    onVideoSelected = {}

                )
            }
        }
    }
    private fun playBackSound() {
        backSound.start()
    }

    private fun playFlipSound() {
        flipSound.start()
    }

    private fun playSwitchSound() {
        switchSound.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        backSound.release()
        flipSound.release()
        switchSound.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    finish: () -> Unit,
    playBackSound: () -> Unit,
    playFlipSound: () -> Unit,
    playSwitchSound: () -> Unit,
    onModelSelected: (ModelItemData) -> Unit,
    onVideoSelected: (VideoItemData) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        playBackSound()
                        finish()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back to Main"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // TabRow for switching tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFFFD700)
                    )
                }
            ) {
                listOf("Models", "Videos").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            playSwitchSound()
                                  },
                        text = { Text(title) }

                    )
                }
            }

            // Content for each tab
            when (selectedTabIndex) {
                0 -> ModelsContent(playFlipSound, onModelSelected)
                1 -> VideosContent(playFlipSound, onVideoSelected)
            }
        }
    }
}
@Composable
fun ModelItem(
    item: ModelItemData,
    playFlipSound: () -> Unit,
    onModelSelected: (ModelItemData) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                playFlipSound()
                onModelSelected(item)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Image(
                painter = painterResource(id = item.previewImageResId),
                contentDescription = "Preview of ${item.name}",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class ModelItemData(
    val name: String,
    val modelPath: String,
    val previewImageResId: Int
)



fun getModelItems(): List<ModelItemData> {
    return listOf(
        //{FIRST BATCH OF 1-9 PAGES}
        //page 223 (num_1)
        ModelItemData("Bacteria", "models/bacteria.glb", R.drawable.bacteria),
        ModelItemData("Amphibian", "models/amphibian.glb", R.drawable.amphibian),
        ModelItemData("Digestive", "models/digestive.glb", R.drawable.digestive),
        ModelItemData("Platypus", "models/platypus.glb", R.drawable.platypus),
        ModelItemData("Heart", "models/heart.glb", R.drawable.heart),
        ModelItemData("Microscope", "models/microscope.glb", R.drawable.microscrope),
        ModelItemData("Earth", "models/earth.glb", R.drawable.earth),
        ModelItemData("Animal", "models/animal.glb", R.drawable.animals),
        ModelItemData("Plant", "models/plant.glb", R.drawable.plant),
        ModelItemData("Biotic", "models/biotic.glb", R.drawable.biotic),
        ModelItemData("Cell", "models/cell.glb", R.drawable.cell),
        ModelItemData("Biodiversity", "models/biodiversity.glb", R.drawable.biodiversity),
        ModelItemData("Organism", "models/organism.glb", R.drawable.organism),
        ModelItemData("Ecosystem", "models/ecosystem.glb", R.drawable.ecosystem),
        ModelItemData("Agriculture", "models/agriculture.glb", R.drawable.agriculture),
        ModelItemData("Medicine", "models/medicine.glb", R.drawable.medicine),

        //page 224 (num_2)
        ModelItemData("Reptile", "models/reptile.glb", R.drawable.reptile),
        ModelItemData("Forest", "models/forest.glb", R.drawable.forest),
        ModelItemData("Ocean", "models/ocean.glb", R.drawable.ocean),

        ModelItemData("River", "models/river.glb", R.drawable.river),

        ModelItemData("Soil", "models/soil.glb", R.drawable.soil),
        ModelItemData("Human", "models/human.glb", R.drawable.human),
        ModelItemData("Acacia", "models/acacia.glb", R.drawable.acacia),
        ModelItemData("Bacterium", "models/bacteria.glb", R.drawable.bacteria),
        ModelItemData("Biological", "models/biological.glb", R.drawable.biological),
        ModelItemData("Specie", "models/specie.glb", R.drawable.specie),

        //page 225 (num_3)
            // [organism] (already implemented)

        //page 226 (num_4)
        ModelItemData("Scientist", "models/scientist.glb", R.drawable.scientist),
        ModelItemData("Classification", "models/classification.glb", R.drawable.classification),
        ModelItemData("Fertile", "models/fertile.glb", R.drawable.fertile_offspring),
        ModelItemData("Domain", "models/domain.glb", R.drawable.three_domains_of_life),
        ModelItemData("Yote", "models/yote.glb", R.drawable.eukaryote),
        ModelItemData("Nucleus", "models/nucleus.glb", R.drawable.nucleus),
        ModelItemData("Chromosome", "models/chromosome.glb", R.drawable.chromosome),
        ModelItemData("Dna", "models/dna.glb", R.drawable.dna),
        ModelItemData("Heredity", "models/heredity.glb", R.drawable.heredity),
        ModelItemData("Membrane", "models/membrane.glb", R.drawable.membrane),
        ModelItemData("Cellular", "models/cellular.glb", R.drawable.cellular),
        ModelItemData("Microorganism", "models/microorganism.glb", R.drawable.microorganism),

        //page 228 (num_6)
        ModelItemData("Methane", "models/methane.glb", R.drawable.methane),

        //page 229 (num_7)
        ModelItemData("Hydrogen", "models/hydrogen.glb", R.drawable.hydrogen),
        ModelItemData("Salinarum", "models/salinarum.glb", R.drawable.salinarum),
        ModelItemData("Halococcus", "models/halococcus.glb", R.drawable.halococcus),

        //-------- 10 Pages [223 - 242] -------------
        // Page 223 (6)
        ModelItemData("Cyano", "models/cyano.glb", R.drawable.cyano),

        // Page 234 (6)
        ModelItemData("Protist", "models/protist.glb", R.drawable.protist),
        ModelItemData("Chlorophyll", "models/chlorophyll.glb", R.drawable.chlorophyll),
        ModelItemData("Dinoflagellate", "models/dinoflagellate.glb", R.drawable.dinoflaglette),
        ModelItemData("Euglanoid", "models/euglanoid.glb", R.drawable.euglanoid),

        // Page 235 (7)
        ModelItemData("Chlorella", "models/chlorella.glb", R.drawable.chlorella),
        ModelItemData("Pediastrum", "models/pediastrum.glb", R.drawable.pediastrum),
        ModelItemData("Spirogyra", "models/spirogyra.glb", R.drawable.spirogyra),

        // Page 237 (8)
        ModelItemData("Eucheuma", "models/eucheuma.glb", R.drawable.eucheuma),
        ModelItemData("Gracilaria", "models/gracilaria.glb", R.drawable.gracilaria),

        // Page 238 (8)
        ModelItemData("Radiolarian", "models/radiolarian.glb", R.drawable.radiolarian),
        ModelItemData("Foraminifera", "models/foraminifera.glb", R.drawable.foraminifera),
        ModelItemData("Amoeba", "models/amoeba.glb", R.drawable.amoeba),

        // Page 239 (9)
        ModelItemData("Paramecium", "models/paramecium.glb", R.drawable.paramecium),
        ModelItemData("Didinium", "models/didinium.glb", R.drawable.didinium),
        ModelItemData("Vorticella", "models/vorticella.glb", R.drawable.vorticella),
        ModelItemData("Slime", "models/slime.glb", R.drawable.slime),
        ModelItemData("Watermold", "models/watermold.glb", R.drawable.watermold),

        // Page 240 (9)
        ModelItemData("Saprophyte", "models/saprophyte.glb", R.drawable.saprophyte),
        ModelItemData("Hyphae", "models/hyphae.glb", R.drawable.hyphae),
        ModelItemData("Rhizoid", "models/rhizoid.glb", R.drawable.rhizoid),

        // Page 242 (10)
        ModelItemData("Vascular", "models/vascular.glb", R.drawable.vascular),

        // Page 242 (10)
        ModelItemData("Peat", "models/peat.glb", R.drawable.peat),

        //10 Pages [243- 262]
        // Page 244 (1)
        ModelItemData("Cot", "models/cot.glb", R.drawable.cot),
        ModelItemData("Sperms", "models/sperms.glb", R.drawable.sperms),

        // Page 244 (2)
        ModelItemData("Leaf venation", "models/leaf venation.glb", R.drawable.leafvenation),
        ModelItemData("Stem", "models/stem.glb", R.drawable.stem),
        ModelItemData("Flower plan", "models/flower plan.glb", R.drawable.flower_plan),

        // Page 247 (3)
        ModelItemData("Harmful plant", "models/harmful plant.glb", R.drawable.harmful_plant),

        // Page 249 (4)
        ModelItemData("Sponge", "models/sponge.glb", R.drawable.sponge),
        ModelItemData("Spiculis", "models/spiculis.glb", R.drawable.spiculis),
        ModelItemData("Osculum", "models/osculum.glb", R.drawable.osculum),
        ModelItemData("Cnidarian", "models/cnidarian.glb", R.drawable.cnidarian),
        ModelItemData("Nematocyst", "models/nematocyst.glb", R.drawable.nematocyst),

        // Page 250 (4)
        ModelItemData("Flatworm", "models/flatworm.glb", R.drawable.flatworm),

        // Page 251 (5)
        ModelItemData("Roundworm", "models/roundworm.glb", R.drawable.roundworm),

        // Page 252 (5)
        ModelItemData("Segmentedworm", "models/segmentedworm.glb", R.drawable.segmentedworm),

        // Page 254 (6)
        ModelItemData("Echinoderm", "models/echinoderm.glb", R.drawable.echinoderm),

        // Page 255 (7)
        ModelItemData("Arthropod", "models/arthropod.glb", R.drawable.arthropod),
        ModelItemData("Exoskeleton", "models/exoskeleton.glb", R.drawable.exoskeleton),
        ModelItemData("Crustacean", "models/crustacean.glb", R.drawable.crustacean),
        ModelItemData("Arachnid", "models/arachnid.glb", R.drawable.arachnid),

        // Page 256 (7)
        ModelItemData("Pede", "models/pede.glb", R.drawable.pede),

        // Page 258 (8)
        ModelItemData("Chordate", "models/chordate.glb", R.drawable.chordate),
        ModelItemData("Turnicate", "models/turnicate.glb", R.drawable.turnicate),
        ModelItemData("Vertebrate", "models/vertebrate.glb", R.drawable.vertebrate),

        //10 Pages [263- 282]
        // Page 254 (1)
        ModelItemData("Eutherians", "models/eutherians.glb", R.drawable.eutherians),
        ModelItemData("Mammal", "models/mammal.glb", R.drawable.mammal),
        ModelItemData("Marsupials", "models/marsupials.glb", R.drawable.marsupials),
        ModelItemData("Monotremes", "models/monotremes.glb", R.drawable.monotremes),

        //--------------[Second Batch] Pages [283- 342]
        //10 Pages [288- 302]
        // Page 264 (1)
        ModelItemData("Monoculture", "models/monoculture.glb", R.drawable.monoculture),

        // Page 291 (5)
        ModelItemData("Digestion", "models/digestion.glb", R.drawable.digestion),

        // Page 300 (9)
           ModelItemData("Bromelain", "models/bromelain.glb", R.drawable.bromelain),

        //10 Pages [303- 322]
        // Page 303 (1)
        ModelItemData("Carbohydrate", "models/carbohydrate.glb", R.drawable.carbohydrate),
        ModelItemData("Lipid", "models/lipid.glb", R.drawable.lipid),
        ModelItemData("Protein", "models/protein.glb", R.drawable.protein),

        // Page 303 (5)
        ModelItemData("Fiber", "models/fiber.glb", R.drawable.fiber),

        //10 Pages [323- 342]
        // Page 327 (3)
        ModelItemData("Spermato", "models/spermato.glb", R.drawable.spermato),

        // Page 328 (3)
        ModelItemData("Down syndrome", "models/down syndrome.glb", R.drawable.down_syndrome),

        // Page 329 (4)
        ModelItemData("Cri du chat", "models/cri du chat.glb", R.drawable.cri_du_chat),

        // Page 327 (4)
        ModelItemData("First filial generation", "models/first filial generation.glb", R.drawable.first_filial_generation),



        )
}

@Composable
fun ModelsContent(
    playFlipSound: () -> Unit,
    onModelSelected: (ModelItemData) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(getModelItems()) { item ->
            ModelItem(item, playFlipSound, onModelSelected)
        }
    }
}

/*Videos Section*/
data class VideoItemData(
    val title: String,
    val videoResId: Int,
 /*   val previewImageResId: Int? = null [Temporary Commented]*/
)
@Composable
fun VideosContent(
    playFlipSound: () -> Unit,
    onVideoSelected: (VideoItemData) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(getVideoItems()) { item ->
            VideoItem(item, playFlipSound, onVideoSelected)
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class) @Composable
fun VideoItem(
    item: VideoItemData,
    playFlipSound: () -> Unit,
    onVideoSelected: (VideoItemData) -> Unit
) {
    val context = LocalContext.current
    val videoPlayerHelper = remember { VideoPlayerHelper(context) }
    val exoPlayer = remember(item.videoResId) {
        videoPlayerHelper.initializePlayer(item.videoResId).apply {
            playWhenReady = true
            volume = 0f
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                playFlipSound()
                onVideoSelected(item)
                val intent = Intent(context, FullscreenVideoActivity::class.java).apply {
                    putExtra("videoResId", item.videoResId)
                }
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        // Set the resize mode to make the video fill the view
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                },
                modifier = Modifier
                    .fillMaxWidth() // Make the AndroidView fill the width of the Column
                    .height(100.dp) // Maintain a fixed height for the preview
            )
        }
    }
}
fun getVideoItems(): List<VideoItemData> {
    return listOf(
        VideoItemData("Classification of Living Things", R.raw.classification),
        VideoItemData("Phyla and It's major Animals", R.raw.phyla),
        VideoItemData("Infertile and Fertile Offsprings", R.raw.fertileoffspring),
        VideoItemData("The Three Domain System", R.raw.threedomainsoflife),
        VideoItemData("What is Biodiversity?", R.raw.biodiversity),
        VideoItemData("Explain Animal Kingdom?", R.raw.animalkingdom),
        VideoItemData("Explain Plant Kingdom?", R.raw.plantkingdom),
        VideoItemData("Enzymes affects digestion", R.raw.enzymes),
        VideoItemData("Human Impact on Ecosystem", R.raw.humanimpact),
        VideoItemData("Am I eating properly?", R.raw.foodpyramid),
        VideoItemData("The Cell Cycle Process", R.raw.cellcycle),
        VideoItemData("What is Nucleus?", R.raw.nucleusmeaning),
        VideoItemData("What is the Cell Division?", R.raw.celldivision),
        VideoItemData("Non-Mendelian Patterns", R.raw.megagenetics),
        VideoItemData("Six Kingdom Classifications", R.raw.sixkingdomsofclassifications),
        VideoItemData("Kingdom of Archaea Bacteria", R.raw.archaekingdom),
        VideoItemData("What is Hydrogen Sulfide?", R.raw.whatishydrogensulfide),
        VideoItemData("What is Cyanobacteria", R.raw.cyanobacteria),
        VideoItemData("What are Angiosperms and Gymnosperms", R.raw.angioandgymno),
        VideoItemData("Monocot and Dicot Difference", R.raw.monocotanddicot),
        VideoItemData("What Harmful plants do to us?", R.raw.harmfulplants),
        VideoItemData("Learn about Aedes Aegypti", R.raw.aedesaegypti),
        VideoItemData("Learn about F1 Generation", R.raw.mendelgeneration),


    )
}
