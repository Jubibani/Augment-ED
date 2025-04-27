package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import android.app.Application
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
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

class RewardsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao()

    private val _rewardItems = MutableStateFlow<List<RewardItemData>>(emptyList())
    val rewardItems: StateFlow<List<RewardItemData>> = _rewardItems.asStateFlow()

    init {
        refreshRewards()
    }

    private fun refreshRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            val miniGames = miniGameDao.getAllMiniGames()
            Log.d("DatabaseDebug", "Fetched from DB: $miniGames")

            _rewardItems.value = miniGames.map { game ->
                when (game.gameId) {
                    "1", "2", "3" -> {
                        RewardItemData(
                            id = game.gameId,
                            name = game.name,
                            description = "Unlock to receive a surprise!",
                            imageResId = R.drawable.question_icon,
                            cost = 50,
                            isUnlocked = game.isUnlocked,
                            onClickAction = {
                                if (game.isUnlocked) {
                                    Toast.makeText(
                                        getApplication(),
                                        "You've unlocked ${game.name}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                    "blueGuy" -> {
                        RewardItemData(
                            id = game.gameId,
                            name = game.name,
                            description = "Unlock to play ${game.name}",
                            imageResId = R.drawable.question_icon,
                            cost = 75,
                            isUnlocked = game.isUnlocked,
                            onClickAction = {
                                if (game.isUnlocked) {
                                    handleMiniGameLaunch(game)
                                } else {
                                    Log.d("MiniGameDebug", "Not enough points to unlock")
                                }
                            }
                        )
                    }
                    else -> {
                        RewardItemData(
                            id = game.gameId,
                            name = game.name,
                            description = "Unlock to play ${game.name}",
                            imageResId = R.drawable.question_icon,
                            cost = 75,
                            isUnlocked = game.isUnlocked,
                            onClickAction = {
                                if (game.isUnlocked) {
                                    handleMiniGameLaunch(game)
                                } else {
                                    Log.d("MiniGameDebug", "Not enough points to unlock")
                                }
                            }
                        )
                    }
                }
            }
            Log.d("DatabaseDebug", "Mapped reward items: ${_rewardItems.value}")
        }
    }

    private fun handleMiniGameLaunch(game: MiniGameEntity) {
        val context = getApplication<Application>()
        val packageName = "com.DefaultCompany.${game.gameId}" // Adjust package name logic as needed

        if (isGameInstalled(context, packageName)) {
            // If the game is installed, launch it
            launchGame(context, packageName)
        } else {
            // If the game is not installed, prompt the user to install it
            Toast.makeText(context, "Game not installed. Installing now...", Toast.LENGTH_SHORT).show()
            val apkFileName = "${game.gameId}.apk" // e.g., "blueGuy.apk"
            val apkFile = copyApkFromAssets(context, apkFileName)
            installApk(context, apkFile)
        }
    }

    private fun isGameInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun launchGame(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Log.e("MiniGameDebug", "Failed to launch game: $packageName")
        }
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

    fun unlockMiniGameAndDeductPoints(gameId: String, cost: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPoints = pointsDao.getPoints()
            if (currentPoints >= cost) {
                pointsDao.updatePoints(currentPoints - cost)
                val miniGame = miniGameDao.getMiniGameById(gameId)

                if (miniGame == null || !miniGame.isUnlocked) {
                    miniGameDao.insertGame(MiniGameEntity(gameId, "Unknown Game", true))
                }

                refreshRewards()
            }
        }
    }

    fun getAllMiniGames(): List<MiniGameEntity> {
        return runBlocking(Dispatchers.IO) {
            miniGameDao.getAllMiniGames()
        }
    }

    fun getMiniGameRewards(): List<RewardItemData> {
        val miniGames = getAllMiniGames()
        return miniGames.map { game ->
            RewardItemData(
                id = game.gameId,
                name = game.name,
                description = "Unlock to play ${game.name}",
                imageResId = R.drawable.question_icon,
                cost = if (game.gameId == "11") 50 else 75,
                isUnlocked = game.isUnlocked,
            ) {
                if (game.isUnlocked) {
                    handleMiniGameLaunch(game)
                } else {
                    Log.d("MiniGameDebug", "Not enough points to unlock")
                }
            }
        }
    }
}