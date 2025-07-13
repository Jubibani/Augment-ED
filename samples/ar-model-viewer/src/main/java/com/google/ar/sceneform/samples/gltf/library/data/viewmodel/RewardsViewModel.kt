package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.MiniGameDao
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.PointsDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.MiniGameEntity
import com.google.ar.sceneform.samples.gltf.library.screens.RewardItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RewardsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao()

    //flags
    val showInfoDialog = MutableStateFlow(false)
    val infoDialogMessage = MutableStateFlow("")

    private val _rewardItems = MutableStateFlow<List<RewardItemData>>(emptyList())
    val rewardItems: StateFlow<List<RewardItemData>> = _rewardItems.asStateFlow()

    init {
        refreshRewards()
    }


    private fun refreshRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Define a mapping of game IDs to their respective costs
                val gameCosts = mapOf(
                    "breakBaller" to 75, // Cost for breakBaller
                    "blueGuy" to 100,
                    "snakeGame" to 85,
                    "rageSailor" to 10,
                )

                // Fetch all mini-games from the database
                val miniGames = miniGameDao.getAllMiniGames()
                Log.d("DatabaseDebug", "Fetched from DB: $miniGames")

                // Map mini-games to RewardItemData
                val rewardItems = miniGames.map { game ->
                    RewardItemData(
                        id = game.gameId,
                        name = game.name,
                        description = "Unlock to play ${game.name}",
                        imageResId = R.drawable.question_icon,
                        cost = gameCosts[game.gameId] ?: 75, // Default cost if not in the map
                        isUnlocked = game.isUnlocked,
                        isInstalled = game.isInstalled,
                        onClickAction = {
                            when {
                                game.isInstalled -> handleMiniGameLaunch(game)
                                game.isUnlocked -> {
                                    Log.d("MiniGameDebug", "Installing game: ${game.name}")
                                    Toast.makeText(
                                        getApplication(),
                                        "Installing ${game.name}...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val apkFileName = "${game.gameId}.apk"
                                    val apkFile = copyApkFromAssets(getApplication(), apkFileName)
                                    installApk(getApplication(), apkFile)
                                }
                                else -> {
                                    Log.d("MiniGameDebug", "Not enough points to unlock ${game.name}")
                                    Toast.makeText(
                                        getApplication(),
                                        "Not enough points to unlock ${game.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }

                // Update the StateFlow on the main thread
                withContext(Dispatchers.Main) {
                    _rewardItems.value = rewardItems
                    Log.d("DatabaseDebug", "Updated reward items: $rewardItems")
                }
            } catch (e: Exception) {
                Log.e("DatabaseDebug", "Error refreshing rewards: ${e.message}")
            }
        }
    }

    private fun handleMiniGameLaunch(game: MiniGameEntity) {
        val context = getApplication<Application>()
        val packageName = "com.DefaultCompany.${game.gameId}"

        Log.d("MiniGameDebug", "Constructed package name: $packageName")

        if (game.isInstalled && isGameInstalled(context, packageName)) {
            Log.d("MiniGameDebug", "Launching game: $packageName")
            launchGame(context, packageName)
        } else {
            Log.d("MiniGameDebug", "Game not installed. Installing: $packageName")
            Toast.makeText(context, "Game not installed. Installing now...", Toast.LENGTH_SHORT).show()
            val apkFileName = "${game.gameId}.apk"
            val apkFile = copyApkFromAssets(context, apkFileName)
            installApk(context, apkFile)
        }
    }

    private fun isGameInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            Log.d("MiniGameDebug", "Game installed status for $packageName: true (using getPackageInfo)")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("MiniGameDebug", "Game installed status for $packageName: false (using getPackageInfo)")
            false
        }
    }



/*    private fun launchGame(context: Context, packageName: String) {
        val activityClassNameGame = "com.unity3d.player.UnityPlayerGameActivity"
        val activityClassName = "com.unity3d.player.UnityPlayerActivity"
        var launchIntent: Intent? = null

        // Try to launch UnityPlayerGameActivity first
        try {
            val componentNameGame = ComponentName(packageName, activityClassNameGame)
            launchIntent = Intent().setComponent(componentNameGame)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Log.d("MiniGameDebug", "Attempting to start activity: $componentNameGame")
            context.startActivity(launchIntent)
            Log.d("MiniGameDebug", "Successfully launched game (GameActivity): $packageName")
            return // Exit the function if successful
        } catch (e: android.content.ActivityNotFoundException) {
            Log.w("MiniGameDebug", "UnityPlayerGameActivity not found. Trying UnityPlayerActivity...")
            launchIntent = null // Reset launchIntent for the next attempt
        } catch (e: Exception) {
            Log.e("MiniGameDebug", "Error attempting to launch UnityPlayerGameActivity: ${e.message}")
            launchIntent = null
        }

        // If UnityPlayerGameActivity failed, try to launch UnityPlayerActivity
        if (launchIntent == null) {
            try {
                val componentName = ComponentName(packageName, activityClassName)
                launchIntent = Intent().setComponent(componentName)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Log.d("MiniGameDebug", "Attempting to start activity: $componentName")
                context.startActivity(launchIntent)
                Log.d("MiniGameDebug", "Successfully launched game (Activity): $packageName")
            } catch (e: android.content.ActivityNotFoundException) {
                Log.e("MiniGameDebug", "Both UnityPlayerGameActivity and UnityPlayerActivity not found for package: $packageName")
            } catch (e: Exception) {
                Log.e("MiniGameDebug", "Error attempting to launch UnityPlayerActivity: ${e.message}")
            }
        }

    }*/

    private fun launchGame(context: Context, packageName: String) {
        // First, try the robust PackageManager method (works for most well-formed APKs)
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Log.d("MiniGameDebug", "Launching via PackageManager: $packageName")
            context.startActivity(launchIntent)
            return
        } else {
            Log.w("MiniGameDebug", "No launch intent from PackageManager, trying known activities...")
        }

        // Fallback: try known main activity classnames (Unity, Godot, etc)
        val activities = listOf(
            "com.unity3d.player.UnityPlayerGameActivity",
            "com.unity3d.player.UnityPlayerActivity",
            "org.godotengine.godot.Godot",
            "com.godot.game.GodotApp" // <-- Godot 4+ export default
        )
        for (activityClass in activities) {
            try {
                val componentName = ComponentName(packageName, activityClass)
                val intent = Intent().setComponent(componentName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Log.d("MiniGameDebug", "Attempting to start activity: $componentName")
                context.startActivity(intent)
                Log.d("MiniGameDebug", "Successfully launched: $componentName")
                return
            } catch (e: android.content.ActivityNotFoundException) {
                Log.w("MiniGameDebug", "$activityClass not found, trying next...")
            } catch (e: Exception) {
                Log.e("MiniGameDebug", "Error launching $activityClass: ${e.message}")
            }
        }
        Log.e("MiniGameDebug", "None of the known activities found for $packageName")
    }

    private fun copyApkFromAssets(context: Context, apkName: String): File {
        val assetManager = context.assets
        val outputFile = File(context.filesDir, apkName)

        assetManager.open("minigames/$apkName").use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return outputFile
    }

    fun unlockMiniGameAndDeductPoints(gameId: String, cost: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPoints = pointsDao.getPoints()
            if (currentPoints >= cost) {
                pointsDao.updatePoints(currentPoints - cost)
                val miniGame = miniGameDao.getMiniGameById(gameId)

                if (miniGame != null) {
                    // Update the existing mini-game to mark it as unlocked
                    miniGameDao.insertGame(miniGame.copy(isUnlocked = true))
                } else {
                    // Insert a new mini-game only if it doesn't exist
                    miniGameDao.insertGame(MiniGameEntity(gameId, name = "Unknown Game", isUnlocked = true, isInstalled = false))
                }

                refreshRewards()
            }
        }
    }



    private fun installApk(context: Context, apkFile: File) {
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)

/*        // Update the database to mark the game as installed
        viewModelScope.launch(Dispatchers.IO) {
            val gameId = apkFile.nameWithoutExtension
            val miniGame = miniGameDao.getMiniGameById(gameId)
            if (miniGame != null) {
                miniGameDao.insertGame(miniGame.copy(isInstalled = true))
                Log.d("MiniGameDebug", "Updated game state in DB: $gameId isInstalled=true")

                // Show the info dialog on the main thread
                withContext(Dispatchers.Main) {
                    showInfoDialog.value = true // Correctly update the StateFlow
                    infoDialogMessage.value = "Go to Home Screen and back here to refresh! "
                }
            }
        }*/
    }
    fun onApkInstalled(gameId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val miniGame = miniGameDao.getMiniGameById(gameId)
            if (miniGame != null && !miniGame.isInstalled) {
                miniGameDao.insertGame(miniGame.copy(isInstalled = true))
                refreshRewards()
            }
        }
    }

    fun showGameInstalledInfo(message: String) {
        showInfoDialog.value = true
        infoDialogMessage.value = message
    }

    fun dismissInfoDialog() {
        showInfoDialog.value = false
    }
}
