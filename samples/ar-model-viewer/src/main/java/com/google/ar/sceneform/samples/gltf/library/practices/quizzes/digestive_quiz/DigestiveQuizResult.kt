package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.digestive_quiz

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.repository.PointsRepository
import com.google.ar.sceneform.samples.gltf.library.screens.PracticeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DigestiveQuizResult : AppCompatActivity() {
    private lateinit var repository: PointsRepository
    private var earnedPoints = 0 // To store the points earned


    private lateinit var popupSound: MediaPlayer
    private lateinit var claimSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_activity_result)

        val userName = intent.getStringExtra(DigestiveConstants.USER_NAME)
        val totalQuestions = intent.getIntExtra(DigestiveConstants.TOTAL_QUESTIONS, 0)
        val score = intent.getIntExtra(DigestiveConstants.SCORE, 0)

        val congratulationsTv: TextView = findViewById(R.id.congratulationsTv)
        val scoreTv: TextView = findViewById(R.id.scoreTv)
   /*     val btnRestart: Button = findViewById(R.id.btnRestart)*/
        val btnExit: Button = findViewById(R.id.btnExit)

        congratulationsTv.text = "Congratulations"
        scoreTv.text = "Your score is $score of $totalQuestions"

        // Initialize the PointsRepository
        val database = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
        repository = PointsRepository(database.brainPointsDao())

        // Calculate earned points and update the database
        earnedPoints = score * 5 // 5 points per correct answer
        updatePoints(earnedPoints)


        //initialize sounds
        popupSound = MediaPlayer.create(this, R.raw.reward)
        claimSound = MediaPlayer.create(this, R.raw.purchase)

        // Play popup sound when the dialog is shown
        val composeView = ComposeView(this).apply {
            setContent {
                var showDialog by remember { mutableStateOf(true) }

                if (showDialog) {
                    // Play popup sound
                    popupSound.start()

                    InfoDialog(
                        earnedPoints = earnedPoints,
                        onDismiss = {
                            showDialog = false
                        },
                        claimSound = claimSound
                    )
                }
            }
        }
        addContentView(
            composeView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Restart button logic: Redirect to the quiz activity
/*        btnRestart.setOnClickListener {
            val intent = Intent(this, DigestiveQuizQuestion::class.java)
            startActivity(intent)
            finish()
        }*/

        // Exit button logic: Redirect to PracticeActivity
        btnExit.setOnClickListener {
            val intent = Intent(this, PracticeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updatePoints(score: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentPoints = repository.getPoints()
            val newPoints = currentPoints + score
            repository.updatePoints(newPoints)
        }
    }
}

@Composable
fun InfoDialog(earnedPoints: Int, onDismiss: () -> Unit, claimSound: MediaPlayer) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Trophy Icon",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Congratulations!",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You earned $earnedPoints points for this quiz!",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Play claim sound
                        claimSound.start()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Claim",
                        color = Color(0xFFFFA500),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}