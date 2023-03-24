package com.example.diceduel

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Collections.min
import kotlin.math.min


class DiceGame : AppCompatActivity() {

    private var gameStats: GameStats? = null

    // Define number of dice and number of sides on each die
    private val numDice = 5
    private val diceSides = 6

    // Declare integer arrays to hold dice rolls for player and computer
    private lateinit var pDiceRolls: IntArray
    private lateinit var cDiceRolls: IntArray

    // Declare ImageView arrays for dice views for player and computer
    private lateinit var pDiceViews: Array<ImageView>
    private lateinit var cDiceViews: Array<ImageView>

    // Declare integer variables for player and computer scores
    private var playerScore = 0
    private var computerScore = 0

    // Declare integer variables for player and computer attempt counts
    private var playerAttempt = 0
    private var computerAttempt = 0

    // Declare TextView for the player and computer score
    private lateinit var computerScoreView: TextView
    private lateinit var playerScoreView: TextView

    // Declare TextView for the player and computer attempt
    private lateinit var playerA: TextView
    private lateinit var computerA: TextView

    // Declare TextView for the player and computer wining
    private lateinit var playerWinsCount: TextView
    private lateinit var computerWinsCount: TextView

    // Declare an ImageView variable for the selected die
    private var selectedDie: ImageView? = null

    // Declare a boolean variable to track whether the score button has been clicked
    private var hasClickedScoreButton = false

    // Declare an integer variable for the number of re-rolls remaining for the player
    private var reRollsCount = 2

    //Declare a text view for the target score
    private lateinit var targetView: TextView

    // Define onCreate function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dice_game)

        gameStats = GameStats()
        loadGameStats()

        playerWinsCount = findViewById(R.id.playerWins)
        computerWinsCount = findViewById(R.id.computerWins)

        playerWinsCount.text = "0"
        computerWinsCount.text="0"

        // Get TextView for target score and set default value to 101
        targetView = findViewById(R.id.target)
        targetView.text = "101"

        // set default value to 0 player and computer attempts
        playerA = findViewById(R.id.playerAttempt)
        playerA.text = "0"
        computerA = findViewById(R.id.compAttempt)
        computerA.text = "0"

        // initialize the TextView variables here
        computerScoreView = findViewById(R.id.computerSum)
        playerScoreView = findViewById(R.id.playerSum)

        // Get TextViews for player and computer scores and set default value to 0
        computerScoreView.text = "0"
        playerScoreView.text = "0"

        // Get buttons for back to home, throw dice, score, and set target
        val throwButton = findViewById<Button>(R.id.throwBtn)
        val scoreButton = findViewById<Button>(R.id.scoreBtn)
        val targetButton = findViewById<Button>(R.id.targetBtn)

        // Disable the score button until the first roll
        scoreButton.isEnabled = false

        // Enable the throw button for the first roll
        throwButton.isEnabled = true

        // Initialize dice rolls arrays for player and computer
        pDiceRolls = IntArray(numDice)
        cDiceRolls = IntArray(numDice)

        // Initialize dice view arrays for player and computer
        pDiceViews = arrayOf(
            findViewById(R.id.pDiceView1),
            findViewById(R.id.pDiceView2),
            findViewById(R.id.pDiceView3),
            findViewById(R.id.pDiceView4),
            findViewById(R.id.pDiceView5)
        )
        cDiceViews = arrayOf(
            findViewById(R.id.cDiceView1),
            findViewById(R.id.cDiceView2),
            findViewById(R.id.cDiceView3),
            findViewById(R.id.cDiceView4),
            findViewById(R.id.cDiceView5)
        )

        // On click listener for the target button
        targetButton.setOnClickListener {
            // Inflate the 'set_target' layout to create the dialog window
            val addTargetWindow = layoutInflater.inflate(R.layout.set_target, null)

            // Create a dialog window
            val addTarget = Dialog(this)
            addTarget.setContentView(addTargetWindow)

            // Set the dialog to be cancelable and set the background to be transparent
            addTarget.setCancelable(true)
            addTarget.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addTarget.show()

            // Get the 'setTargetBtn' button from the dialog window and set an on click listener
            val setTargetBtn = addTargetWindow.findViewById<Button>(R.id.setTargetBtn)
            setTargetBtn.setOnClickListener {
                // When the 'setTargetBtn' button is clicked, get the text from the 'targetEditText'
                // input field and set the 'targetView' text to be that text
                val targetEditText = addTargetWindow.findViewById<EditText>(R.id.targetEditText)
                val targetValue = targetEditText.text.toString()
                targetView.text = targetValue

                if (targetView.text.equals("")) {

                    // If the target is empty, show a pop up window asking to set a target
                    val targetPopupWindow =
                        layoutInflater.inflate(R.layout.target_empty_popup_window, null)

                    val targetInfo = Dialog(this)
                    targetInfo.setContentView(targetPopupWindow)

                    targetInfo.setCancelable(true)

                    targetInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    targetInfo.show()

                    val targetPopupWindowOkBtn =
                        targetPopupWindow.findViewById<Button>(R.id.targetEmptyOkBtn)
                    targetPopupWindowOkBtn.setOnClickListener {
                        targetInfo.dismiss()
                    }
                    throwButton.isEnabled = false
                }

                if (!(targetView.text.equals(""))) {
                    throwButton.isEnabled = true
                }

                // Dismiss the dialog window
                addTarget.dismiss()
            }
        }

        // On click listener for the throw button
        throwButton.setOnClickListener {
            if (hasClickedScoreButton) {
                if (selectedDie == null) {

                    // If the user has not selected a die, show a pop up window asking them to do so
                    val diceSelectPopupWindow =
                        layoutInflater.inflate(R.layout.dice_select_info_popup_window, null)

                    val diceSelectInfo = Dialog(this)
                    diceSelectInfo.setContentView(diceSelectPopupWindow)

                    diceSelectInfo.setCancelable(true)

                    diceSelectInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    diceSelectInfo.show()

                    val diceSelectPopupWindowOkBtn =
                        diceSelectPopupWindow.findViewById<Button>(R.id.selectDiceOkBtn)
                    diceSelectPopupWindowOkBtn.setOnClickListener {
                        diceSelectInfo.dismiss()
                    }
                } else if (reRollsCount > 0) {
                    // If the user has selected a die and still has rerolls remaining,
                    // re-roll the unselected dice and decrement the rerolls count by 1
                    reRollPlayerDices()
                    scoreButton.isEnabled = true
                    throwButton.isEnabled = false
                    reRollsCount--
                    playerAttempt++

                    // Update the player's attempt count
                    playerA.text = playerAttempt.toString()

                    if (reRollsCount == 0) {
                        // If the user has used up all their rerolls, add the score of the
                        // remaining dice rolls to their score and show an alert dialog telling
                        // them they cannot reroll anymore
                        playerScore += pDiceRolls.sum()
                        playerScoreView.text = playerScore.toString()

                        val rerollPopupWindow =
                            layoutInflater.inflate(R.layout.reroll_popup_window, null)

                        val rerollInfo = Dialog(this)
                        rerollInfo.setContentView(rerollPopupWindow)

                        rerollInfo.setCancelable(true)

                        rerollInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        rerollInfo.show()

                        val rerollPopupWindowOkBtn =
                            rerollPopupWindow.findViewById<Button>(R.id.rerollDiceOkBtn)
                        rerollPopupWindowOkBtn.setOnClickListener {
                            rerollInfo.dismiss()
                        }

                        scoreButton.isEnabled = false
                        throwButton.isEnabled = true
                    }
                }
            } else {
                // If the user has not clicked the score button yet, roll the dice
                rollDice()
                scoreButton.isEnabled = true
                throwButton.isEnabled = false
                // reset selected die after the first roll
                selectedDie = null

                // Update the player's attempt count
                playerAttempt++

                computerA.text = computerAttempt.toString()
                playerA.text = playerAttempt.toString()
            }

            // Get the target score from the 'targetView' and convert it to an integer
            var targetScore = (targetView.text as String).toInt()
            var pCheckScore = playerScore

            pCheckScore += pDiceRolls.sum()

            if ((playerAttempt == computerAttempt) && (pCheckScore == computerScore) && (pCheckScore == targetScore) && (computerScore == targetScore)) {

                // tie Round PopUp Window
                val tiePopupWindow = layoutInflater.inflate(R.layout.tie_popup_window, null)

                val tieInfo = Dialog(this)
                tieInfo.setContentView(tiePopupWindow)

                tieInfo.setCancelable(true)

                tieInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                tieInfo.show()

                val tiePopupWindowOkBtn = tiePopupWindow.findViewById<Button>(R.id.tieOkBtn)
                tiePopupWindowOkBtn.setOnClickListener {
                    tieInfo.dismiss()
                    optionalRollDice()
                }
            }
            // Check if either player has won
            if (((pCheckScore >= targetScore) && (computerScore < pCheckScore)) || ((computerScore == pCheckScore) && (playerAttempt < computerAttempt))) {
                // Player wins
                playerScoreView.text = pCheckScore.toString()
                computerScoreView.text = computerScore.toString()
                computerA.text = computerAttempt.toString()
                playerA.text = playerAttempt.toString()
                scoreButton.isEnabled = false
                throwButton.isEnabled = false

                //Added to the player wins count
                gameStats?.humanWins = gameStats?.humanWins?.plus(1) ?: 1

                // Update the win counts
                playerWinsCount.text = gameStats?.humanWins.toString()


                // Player Win PopUp Window
                val winPopupWindow = layoutInflater.inflate(R.layout.win_popup_window, null)

                val winInfo = Dialog(this)
                winInfo.setContentView(winPopupWindow)

                winInfo.setCancelable(true)

                winInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                winInfo.show()

                val winPopupWindowOkBtn =
                    winPopupWindow.findViewById<Button>(R.id.winOkBtn)
                winPopupWindowOkBtn.setOnClickListener {
                    winInfo.dismiss()
                }
            }

            if ((computerScore >= targetScore) && (pCheckScore < computerScore) || ((computerScore == pCheckScore) && (playerAttempt > computerAttempt))) {
                // Computer wins
                computerScoreView.text = computerScore.toString()
                playerScoreView.text = pCheckScore.toString()
                computerA.text = computerAttempt.toString()
                playerA.text = playerAttempt.toString()
                scoreButton.isEnabled = false
                throwButton.isEnabled = false

                //Added to the computer player wins count
                gameStats?.computerWins = gameStats?.computerWins?.plus(1) ?: 1

                // Update the win counts
                computerWinsCount.text = gameStats?.computerWins.toString()


                // Player Lose PopUp Window
                val losePopupWindow = layoutInflater.inflate(R.layout.lose_popup_window, null)

                val loseInfo = Dialog(this)
                loseInfo.setContentView(losePopupWindow)

                loseInfo.setCancelable(true)

                loseInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                loseInfo.show()

                val losePopupWindowOkBtn = losePopupWindow.findViewById<Button>(R.id.loseOkBtn)
                losePopupWindowOkBtn.setOnClickListener {
                    loseInfo.dismiss()
                }
            }
            // Disable the target button
            targetButton.isEnabled = false
        }

        // On click listener for the score button
        scoreButton.setOnClickListener {

            // Update the computer score text view with the current computer score
            computerScoreView.text = computerScore.toString()

            // Inflate the layout for the score popup window
            val scorePopupWindow = layoutInflater.inflate(R.layout.score_popup_window, null)

            // Create a dialog to display the score popup window
            val scoreInfo = Dialog(this)
            scoreInfo.setContentView(scorePopupWindow)

            // Make the dialog dismissible when the user taps outside of it
            scoreInfo.setCancelable(true)

            // Make the dialog's background transparent
            scoreInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Show the score popup window dialog
            scoreInfo.show()

            // Set up the click listener for the "Yes" button in the score popup window
            val scorePopupWindowYesBtn = scorePopupWindow.findViewById<Button>(R.id.scoreInfoYesBtn)
            scorePopupWindowYesBtn.setOnClickListener {

                // Dismiss the score popup window dialog
                scoreInfo.dismiss()

                // Add the sum of the player's dice rolls to their score
                playerScore += pDiceRolls.sum()

                // Update the computer score and player score text views with the new scores
                computerScoreView.text = computerScore.toString()
                playerScoreView.text = playerScore.toString()

                // Enable the "Throw" button and disable the "Score" button
                throwButton.isEnabled = true
                scoreButton.isEnabled = false

                // Reset the re-rolls counter to 2
                reRollsCount = 2
            }

            // Set up the click listener for the "No" button in the score popup window
            val scorePopupWindowNoBtn = scorePopupWindow.findViewById<Button>(R.id.scoreInfoNoBtn)
            scorePopupWindowNoBtn.setOnClickListener {

                // Dismiss the score popup window dialog
                scoreInfo.dismiss()

                // Enable the "Throw" button and disable the "Score" button
                throwButton.isEnabled = true
                scoreButton.isEnabled = false

                // Check if there are any re-rolls remaining
                if (reRollsCount > 0) {
                    // Enable the player to select which dice to re-roll
                    enableDiceSelection()

                    // Set the "has clicked score button" flag to true
                    hasClickedScoreButton = true
                } else {
                    // Set the "has clicked score button" flag to false
                    hasClickedScoreButton = false

                    // Reset the re-rolls counter to 2
                    reRollsCount = 2
                }
            }
        }

    }

    private fun loadGameStats() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        gameStats?.humanWins = sharedPreferences.getInt("humanWins", 0)
        gameStats?.computerWins = sharedPreferences.getInt("computerWins", 0)

        if (gameStats?.humanWins == null || gameStats?.computerWins == null) {
            gameStats?.reset()
            saveGameStats()
        }

        updateWinCount()
    }

    override fun onStop() {
        saveGameStats()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        val prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("humanWins", gameStats?.humanWins ?: 0)
        editor.putInt("computerWins", gameStats?.computerWins ?: 0)
        editor.apply()
    }


    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val justOpened = prefs.getBoolean("justOpened", true)

        if (justOpened) {
            // Reset the win counts to 0
            gameStats?.humanWins = 0
            gameStats?.computerWins = 0

            // Update the TextViews with the new win counts
            playerWinsCount.text = "0"
            computerWinsCount.text = "0"

            // Set justOpened to false
            prefs.edit().putBoolean("justOpened", false).apply()
        } else {
            val humanWins = prefs.getInt("humanWins", 0)
            val computerWins = prefs.getInt("computerWins", 0)
            gameStats?.humanWins = humanWins
            gameStats?.computerWins = computerWins
            playerWinsCount.text = humanWins.toString()
            computerWinsCount.text = computerWins.toString()
        }
    }

    private fun updateWinCount() {
        playerWinsCount = findViewById(R.id.playerWins)
        computerWinsCount = findViewById(R.id.computerWins)
        playerWinsCount.text = gameStats?.humanWins.toString()
        computerWinsCount.text = gameStats?.computerWins.toString()
    }

    private fun saveGameStats() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("humanWins", gameStats?.humanWins ?: 0)
            putInt("computerWins", gameStats?.computerWins ?: 0)
            apply()
        }
    }

    override fun onBackPressed() {
        // Save the game stats when the user presses the back button
        saveGameStats()
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        if (gameStats !== null) {
            playerWinsCount.text = "0"
            computerWinsCount.text = "0"
            gameStats = GameStats()
        }
    }


    /*
     * This function implements the computer player's strategy for rolling the dice in the game. The strategy determines

     * whether the computer player should reroll some of its dice and which ones, based on the sum of the current dice rolls

     * and the scores of both the computer player and the human player. The function allows for up to two rerolls, and

     * updates the computer player's score accordingly.

     * The strategy works as follows:

     * If the sum of the current dice rolls is already within the desired range (18 to 30), the computer player does
     * not reroll any dice.

     * If the sum of the current dice rolls is too low (10 to 17), or if the computer player is losing by more than 25
     * points, the computer player rerolls one of its dice and keeps the highest value.

     * If the computer player is tied with the human player, it does not reroll any dice.
     * Otherwise, if the computer player is losing by less than 25 points, or if it is winning, the computer player
     * rerolls two of its dice and keeps the two highest values.

     * Advantages of this strategy include its simplicity and flexibility, allowing for adaptation to different game states.

     * It also prioritizes maximizing the computer player's score while minimizing the risk of losing points.
     *
     * One disadvantage could be the lack of consideration for the human player's score, as the computer player does not have access to this
    */

    private fun rollDice() {
        var targetScore = (targetView.text as String).toInt()

        // Roll the player's dice and store the results in an integer array
        pDiceRolls = IntArray(numDice) { Dice(diceSides).roll() }

        // Update the player's dice images based on the roll results
        updateDiceViews(pDiceViews, pDiceRolls)

        // Roll the computer's dice and store the results in an integer array
        cDiceRolls = IntArray(numDice) { Dice(diceSides).roll() }

        // Calculate the sum of the computer's dice rolls and update the computer's attempt count
        var cDiceViewSum = cDiceRolls.sum()
        computerAttempt++

        // Check the computer's dice roll sum and determine the strategy to use
        val playerScoreDiff = playerScore - targetScore
        val computerScoreDiff = computerScore - targetScore

        when {
            cDiceViewSum in 18..30 -> {
                // Do nothing, since the dice roll sum is already within the desired range
                updateDiceViews(cDiceViews, cDiceRolls)
            }
            cDiceViewSum in 10..17 || computerScoreDiff >= 25 -> {
                // Reroll the computer's dice once and keep the highest value
                val maxDiceValue = cDiceRolls.max()!!
                val rerollIndices = cDiceRolls.withIndex().filter { it.value != maxDiceValue }.map { it.index }
                cDiceRolls = IntArray(numDice) { i ->
                    if (rerollIndices.contains(i)) {
                        Dice(diceSides).roll()
                    } else {
                        cDiceRolls[i]
                    }
                }
                // Update the CD dice view with the new cDiceRolls array
                updateDiceViews(cDiceViews, cDiceRolls)
                cDiceViewSum = cDiceRolls.sum()
                computerAttempt++
            }
            playerScoreDiff == computerScoreDiff -> {
                // If the scores are tied, don't reroll
                updateDiceViews(cDiceViews, cDiceRolls)
            }
            else -> {
                // Reroll the computer's dice twice and keep the two highest values
                val maxDiceValues = cDiceRolls.sortedDescending().take(2)
                val rerollIndices = cDiceRolls.withIndex().filter { !maxDiceValues.contains(it.value) }.map { it.index }
                cDiceRolls = IntArray(numDice) { i ->
                    if (rerollIndices.contains(i)) {
                        Dice(diceSides).roll()
                    } else {
                        cDiceRolls[i]
                    }
                }
                // Update the CD dice view with the new cDiceRolls array
                updateDiceViews(cDiceViews, cDiceRolls)
                cDiceViewSum = cDiceRolls.sum()
                computerAttempt += 2
            }
        }

        // Update the computer's score after the dice rolls and rerolls have been made
        computerScore += cDiceViewSum

    }


    // This function allows the player to re-roll any dice that they did not select
    private fun reRollPlayerDices() {
        // Reset the flag that tracks whether the player has clicked the Score button
        hasClickedScoreButton = false

        // Re-roll any dice that the player did not select
        for (i in pDiceViews.indices) {
            if (!pDiceViews[i].isSelected) {
                pDiceRolls[i] = Dice(diceSides).roll()
            }
        }

        // Update the player's dice images with the new rolls
        updateDiceViews(pDiceViews, pDiceRolls)

        // Reset the selected die and disable further dice selection
        // Change the drawable background of the selected die
        selectedDie?.background = getDrawable(R.drawable.dice_background)
        selectedDie = null
        disableDiceSelection()
    }


    /*
    * This function updates the views of the dice based on the values rolled
    * by each die. The diceViews array contains the ImageViews of each die,
    * and the diceRolls array contains the values rolled by each die.
    */
    private fun updateDiceViews(diceViews: Array<ImageView>, diceRolls: IntArray) {
        for (i in diceRolls.indices) {
            val diceView = diceViews[i]
            val diceRoll = diceRolls[i]

            // Assigns a drawable ID based on the diceRoll value.
            // Each drawable represents a different face of the dice.
            val drawableId = when (diceRoll) {
                1 -> R.drawable.dice_1
                2 -> R.drawable.dice_2
                3 -> R.drawable.dice_3
                4 -> R.drawable.dice_4
                5 -> R.drawable.dice_5
                6 -> R.drawable.dice_6
                else -> 0
            }

            // Sets the drawable of the diceView to the drawable with the
            // corresponding drawable ID, if the ID is not 0 (which means there
            // is no drawable for that value of diceRoll).
            if (drawableId != 0) {
                diceView.setImageResource(drawableId)
            }
        }
    }

    /*
    * Enables the user to select a single die by clicking on it. When a die
    * is selected, its isSelected property is set to true, and the selectedDie
    * variable is updated to point to the selected die. If a die was already
    * selected, its isSelected property is set to false and selectedDie is set to null.
    */
    private fun enableDiceSelection() {
        val originalBackgrounds = mutableMapOf<ImageView, Drawable>()
        pDiceViews.forEach { die ->
            originalBackgrounds[die] = die.background
            die.isClickable = true
            die.setOnClickListener {
                if (selectedDie == die) {
                    selectedDie?.isSelected = false
                    selectedDie?.background = originalBackgrounds[die]
                    selectedDie = null
                } else {
                    selectedDie?.isSelected = false
                    selectedDie?.background = originalBackgrounds[die]
                    die.isSelected = true
                    die.setBackgroundResource(R.drawable.dice_select_background)
                    selectedDie = die
                }
            }
        }
    }

    /*
    * Disables the user's ability to select a die by setting each die's
    * isClickable property to false and removing its OnClickListener.
    * Also sets the isSelected property of each die to false.
    */
    private fun disableDiceSelection() {
        pDiceViews.forEach { die ->
            die.isClickable = false
            die.setOnClickListener(null)
            die.isSelected = false
        }
    }

    private fun optionalRollDice() {
        var hasWinner = false

        playerScore += pDiceRolls.sum()

        var pTCheckScore = playerScore

        // create new arrays to store dice rolls for player and computer
        var opPDiceRolls = IntArray(numDice) { Dice(diceSides).roll() }
        var opCDiceRolls = IntArray(numDice) { Dice(diceSides).roll() }

        // update the views to show the new dice rolls
        updateDiceViews(pDiceViews, opPDiceRolls)
        updateDiceViews(pDiceViews, opCDiceRolls)

        while (!hasWinner) {
            // player rolls dice
            val playerRoundScore = opPDiceRolls.sum()
            pTCheckScore += playerRoundScore
            playerAttempt++

            // computer rolls dice
            val computerRoundScore = opCDiceRolls.sum()
            computerScore += computerRoundScore
            computerAttempt++

            // compare scores to see if there's a winner
            if (pTCheckScore > computerScore) {
                hasWinner = true

                computerScoreView.text = computerScore.toString()
                playerScoreView.text = pTCheckScore.toString()
                computerA.text = computerAttempt.toString()
                playerA.text = playerAttempt.toString()

                //Added to the player wins count
                gameStats?.humanWins = gameStats?.humanWins?.plus(1) ?: 1

                // Update the win counts
                playerWinsCount.text = gameStats?.humanWins.toString()

                // Player Win PopUp Window
                val winPopupWindow = layoutInflater.inflate(R.layout.win_popup_window, null)

                val winInfo = Dialog(this)
                winInfo.setContentView(winPopupWindow)

                winInfo.setCancelable(true)

                winInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                winInfo.show()

                val winPopupWindowOkBtn =
                    winPopupWindow.findViewById<Button>(R.id.winOkBtn)
                winPopupWindowOkBtn.setOnClickListener {
                    winInfo.dismiss()
                }

            }
            else if (computerScore > pTCheckScore) {
                hasWinner = true

                computerScoreView.text = computerScore.toString()
                playerScoreView.text = pTCheckScore.toString()
                computerA.text = computerAttempt.toString()
                playerA.text = playerAttempt.toString()

                //Added to the computer player wins count
                gameStats?.computerWins = gameStats?.computerWins?.plus(1) ?: 1

                // Update the win counts
                computerWinsCount.text = gameStats?.computerWins.toString()

                // Player Lose PopUp Window
                val losePopupWindow = layoutInflater.inflate(R.layout.lose_popup_window, null)

                val loseInfo = Dialog(this)
                loseInfo.setContentView(losePopupWindow)

                loseInfo.setCancelable(true)

                loseInfo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                loseInfo.show()

                val losePopupWindowOkBtn = losePopupWindow.findViewById<Button>(R.id.loseOkBtn)
                losePopupWindowOkBtn.setOnClickListener {
                    loseInfo.dismiss()
                }
            }
        }
    }

    /*
    * A Dice class that represents a single die with a given number of sides.
    * The roll() function returns a random number between 1 and the number of
    * sides, simulating a dice roll.
    */
    class Dice(val diceNoSides: Int) {
        fun roll(): Int {
            return (1..diceNoSides).random()
        }
    }
}

