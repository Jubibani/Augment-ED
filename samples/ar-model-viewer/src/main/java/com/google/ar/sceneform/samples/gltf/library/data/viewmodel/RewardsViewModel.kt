package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RewardsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao()

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
                val gameCosts = mapOf(
                    "breakBaller" to 75,
                    "blueGuy" to 50,
                    "snakeGame" to 85,
                    "rageSailor" to 10,
                    "flyingBlock" to 10,
                )

                val context = getApplication<Application>()
                val miniGames = miniGameDao.getAllMiniGames()
                Log.d("DatabaseDebug", "Fetched from DB: $miniGames")

                // Synchronize installed status with real device state
                miniGames.forEach { game ->
                    val packageName = "com.DefaultCompany.${game.gameId}"
                    val actuallyInstalled = isGameInstalled(context, packageName)
                    if (game.isInstalled != actuallyInstalled) {
                        miniGameDao.insertGame(game.copy(isInstalled = actuallyInstalled))
                        Log.d("DatabaseDebug", "Updated ${game.gameId} install status to $actuallyInstalled")
                    }
                }

                // Fetch again after possible updates
                val updatedGames = miniGameDao.getAllMiniGames()
                val rewardItems = updatedGames.map { game ->
                    RewardItemData(
                        id = game.gameId,
                        name = game.name,
                        description = "Unlock to play ${game.name}",
                        imageResId = R.drawable.question_icon,
                        cost = gameCosts[game.gameId] ?: 75,
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


            refreshRewards()

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
            Log.d("MiniGameDebug", "Game installed status for $packageName: true")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("MiniGameDebug", "Game installed status for $packageName: false")
            false
        }
    }

    private fun launchGame(context: Context, packageName: String) {
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

        val activities = listOf(
            "com.unity3d.player.UnityPlayerGameActivity",
            "com.unity3d.player.UnityPlayerActivity",
            "org.godotengine.godot.Godot",
            "com.godot.game.GodotApp"
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
                    miniGameDao.insertGame(miniGame.copy(isUnlocked = true))
                } else {
                    miniGameDao.insertGame(MiniGameEntity(gameId, name = "Unknown Game", isUnlocked = true, isInstalled = false))
                }

                refreshRewards()
            }
        }
    }

    // Only intent logic here, no permission check
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