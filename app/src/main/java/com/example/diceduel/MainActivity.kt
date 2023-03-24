package com.example.diceduel

import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var gameStats: GameStats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameStats = GameStats()
        loadGameStats()

        // Get references to the buttons on the main activity layout
        var newGameButton = findViewById<Button>(R.id.newGameBtn)
        val aboutButton = findViewById<Button>(R.id.aboutBtn)

        // Set up a click listener for the "New Game" button
        newGameButton.setOnClickListener {
            // When the button is clicked, create a new Intent to launch the game page activity
            val intent = Intent(this, DiceGame::class.java)
            intent.putExtra("gameStats", gameStats)
            startActivity(intent)
        }

        // Set up a click listener for the "About" button
        aboutButton.setOnClickListener {
            // When the button is clicked, inflate the about popup window layout
            val dialogBinding = layoutInflater.inflate(R.layout.about_popup_window, null)

            // Create a new Dialog object and set its content view to the inflated layout
            val aboutMe = Dialog(this)
            aboutMe.setContentView(dialogBinding)

            // Set the dialog to be cancelable and set its background to be transparent
            aboutMe.setCancelable(true)
            aboutMe.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            aboutMe.show()

            // Get a reference to the "OK" button in the popup window and set up a click listener for it
            val popupWindowOkBtn = dialogBinding.findViewById<Button>(R.id.popup_window_ok_btn)
            popupWindowOkBtn.setOnClickListener {
                // When the "OK" button is clicked, dismiss the popup window
                aboutMe.dismiss()
            }
        }
    }


    private fun saveGameStats() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("humanWins", gameStats.humanWins)
            putInt("computerWins", gameStats.computerWins)
            apply()
        }
    }

    private fun loadGameStats() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        gameStats.humanWins = sharedPreferences.getInt("humanWins", 0)
        gameStats.computerWins = sharedPreferences.getInt("computerWins", 0)
        saveGameStats()
    }

    private fun resetGameStats() {
        gameStats.reset()
    }

    override fun onPause() {
        super.onPause()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val isAppInBackground = activityManager.runningAppProcesses.none {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }

        if (isAppInBackground) {
            resetGameStats()
        }
    }
}

private fun Intent.putExtra(s: String, gameStats: GameStats) {

}