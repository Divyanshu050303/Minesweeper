package com.example.minesweeper

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    //true if user chooses to create custom board
    var custom = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get best time and lst game time from shared preferences
        val sharedPreferences = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val bestTime: Int = sharedPreferences.getInt("BEST_TIME", 0)
        val lastGameTime: Int = sharedPreferences.getInt("LAST_GAME_TIME", 0)

        //set the best time and last game time in their respective text views
        tvbestTime.text = "Best Time: ${bestTime}s"
        lastTime.text = "Last Game Time: ${lastGameTime}s"

        customButton.setOnClickListener {
            //Toggle the visibility of EditTexts on each click
            if (!custom) {
                etColumns.visibility = View.VISIBLE
                etRows.visibility = View.VISIBLE
                etMines.visibility = View.VISIBLE
            } else {
                etColumns.visibility = View.INVISIBLE
                etRows.visibility = View.INVISIBLE
                etMines.visibility = View.INVISIBLE
            }
            custom = !custom
        }

        btnstart.setOnClickListener {
            //if custom is true, it means user chooses to make custom board
            //else user chooses between easy, medium and hard levels
            if (custom) {
                makeCustomBoard()
            } else {
                getSelectedLevel()
            }
        }
    }

    private fun getSelectedLevel() {
        /*
        Get the checked radio button
        By default,
        for easy mode, rows=9, columns=9, mines=20
        for medium mode, rows=16, columns=16, mines=63
        for hard mode, rows=30, columns=16, mines=119
        and invoke startGame function which take rows, columns and mines as parameters
         */
        when (difficultyButton.checkedRadioButtonId) {
            easy.id ->
                startGame(9, 9, 20)
            medium.id ->
                startGame(16, 16, 63)
            hard.id ->
                startGame(30, 16, 119)
        }
    }

    private fun makeCustomBoard() {
        //Check if any edit text is blank or not
        if (etRows.text.toString().isBlank()) {
            etRows.error = "Required Field"
            return
        }
        if (etColumns.text.toString().isBlank()) {
            etColumns.error = "Required Field"
            return
        }
        if (etMines.text.toString().isBlank()) {
            etMines.error = "Required Field"
            return
        }

        //get the number of rows, columns and mines
        val rows = Integer.parseInt(etRows.text.toString())
        val columns = Integer.parseInt(etColumns.text.toString())
        val mines = Integer.parseInt(etMines.text.toString())

        //check if the number of mines is less than 1/4th of the board’s button to avoid
        // overcrowding of mines.
        val threshold = (rows * columns) / 4
        if (mines >= threshold) {
            etMines.error = "Mines should be less than $threshold"
            return
        }

        //Invoke startGame function which take rows, columns and mines as parameters
        startGame(rows, columns, mines)
    }

    private fun startGame(rows: Int, columns: Int, mines: Int) {
        //Start the GameActivity and pass it number of rows, columns and mines
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("ROWS", rows)
            putExtra("COLUMNS", columns)
            putExtra("MINES", mines)
        }
        startActivity(intent)
    }
}