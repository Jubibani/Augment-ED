package com.google.ar.sceneform.samples.gltf.library.screens

import LibraryFragment
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

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    finish: () -> Unit,
    playBackSound: () -> Unit,
    playFlipSound: () -> Unit,
    onModelSelected: (ModelItemData) -> Unit
) {
    val context = LocalContext.current
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
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
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
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(getModelItems()) { item ->
                ModelItem(item, playFlipSound, onModelSelected)
            }
        }
    }
}
*/

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

/* [Old Code]
val modelActivityMap = mapOf(
    "Amphibian" to AmphibianActivity::class.java,
    "Bacteria" to BacteriaActivity::class.java,
    "Digestive System" to DigestiveActivity::class.java,
    "Platypus" to PlatypusActivity::class.java,
    "Heart" to HeartActivity::class.java
    // Add more mappings as needed
)*/

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


        // Add more items as needed
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




        // Add more videos as needed, using R.raw.<your_video_file_name>
    )
}
