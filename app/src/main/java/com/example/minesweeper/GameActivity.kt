package com.example.minesweeper


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.game_activity.*
import java.util.*

@Suppress("DEPRECATION")
class GameActivity:AppCompatActivity() {
    private val MINE=-1

    private var rows:Int =9
    private var columns:Int =9
    private var mines:Int =20

    private var count:Int=0
    private var flagCount:Int=0
    private var time="0"

    private lateinit var board:Array<Array<MineButton>>
    private var flag =false
    private val movement= intArrayOf(-1, 0, 1)

    private var second=0
    private var running=false
    private var wasRunning=false
    private val handler= Handler()
    private lateinit var runnabel:Runnable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)
        startTimer()
        rows=intent.getIntExtra("ROWS", 9)
        columns=intent.getIntExtra("COLUMNS", 9)
        mines=intent.getIntExtra("MINES", 20)

        board= Array(rows){ Array(columns){ MineButton() } }

        setupBoard()

        restart.setOnClickListener{
            restartGame()
        }
        imgflag.setOnClickListener{
            if(flag)
                imgflag.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.flag))
            else
                imgflag.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mine))
            flag=!flag
        }
    }
    private fun startTimer(){
        runnabel= Runnable { doJob() }
        handler.post(runnabel)

    }
    private fun doJob(){
        val hour:Int =second/3600
        val minutes:Int=second%3600/60
        val secs:Int=second%60
        time =String.format(
            Locale.getDefault(),"%d:%02d:%02d",hour, minutes, secs
        )
        tvtime.text=time
        if(running)
            second++
        handler.postDelayed(runnabel, 1000)
    }
    private fun setupBoard(){
        val params1=LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0
        )
        val params2=LinearLayout.LayoutParams(0, 150)
        for(i in 0 until rows){
            val linearLayout=LinearLayout(this)
            linearLayout.orientation=LinearLayout.HORIZONTAL
            linearLayout.layoutParams=params1
            params1.weight=1.0F
            for(j in 0 until columns){
                val button=Button(this)
                button.layoutParams=params2
                params2.weight=1.0F
                button.setBackgroundColor(R.drawable.button)
                button.setOnClickListener{
                    recordMode(i, j)
                }
                linearLayout.addView(button)
            }
            llBoard.addView(linearLayout)
        }
        flagCount=mines
        tvmenis.text="$flagCount"
        tvtime.text= time
    }
    private fun recordMode(x:Int, y:Int){
        val button=getButton(x, y)
        if(count==0){
            count++
            running=true
            setMines()
            for(i in movement)
                for(j in movement)
                    if(!(i==0 && j==0)&&((x+i) in 0 until rows)&&((y+j)in 0 until columns))
                        reveal(x+i, y+j)
        }
        if(flag){
            if(board[x][y].isMarked){
                board[x][y].isMarked=!board[x][y].isMarked
                button.text=""
                button.setBackgroundResource(R.drawable.button)
                flagCount++
                tvmenis.text="$flagCount"
            }
            else{
                if(flagCount>0){
                    board[x][y].isMarked=!board[x][y].isMarked
                    button.setBackgroundResource(R.drawable.button)
                    button.background=ContextCompat.getDrawable(this, R.drawable.flag)
                    flagCount--
                    tvmenis.text="$flagCount"
                }
            }
        }
        else{
            if(board[x][y].isMarked||board[x][y].isRevealed){
                return
            }
            if(board[x][y].value==MINE){
                gameLost()
            }
            else{
                reveal(x, y)
            }
        }
        if(isComplete()){
            running=false
            disableAllButton()
            Toast.makeText(this, "Congratulation! you won.", Toast.LENGTH_LONG).show()
            updateScore("Won")
            showDialog()
        }

    }
    private fun getButton(x:Int , y:Int ):Button{
        val rowLayout=llBoard.getChildAt(x) as LinearLayout
        return  rowLayout.getChildAt(y) as Button
    }
    private fun setMines(){
        var i=1
        while(i<=mines){
            val x=(0 until rows).random()
            val y =(0 until columns).random()
            if(x!=rows && y!=columns && board[x][y].value!=MINE){
                board[x][y].value=MINE
                updateNeghbouring(x, y)
                i++
            }
        }
    }
    private fun updateNeghbouring(row:Int, column:Int){
        for(i in movement){
            for(j in movement){
                if(((row+i)in 0 until rows)&& ((column+j)in 0 until columns)&& board[row+i][column+j].value!=MINE)
                    board[row+i][column+j].value++
            }
        }
    }
    private fun reveal(x:Int , y:Int){
        if(!board[x][y].isRevealed && !board[x][y].isMarked && board[x][y].value!=MINE){
            val button=getButton(x, y)
            button.text=board[x][y].value.toString()
            button.isEnabled=false
            board[x][y].isRevealed=true
            button.setBackgroundResource(R.drawable.disabled_button)
            button.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
            if(board[x][y].value==0){
                for(i in movement){
                    for(j in movement){
                        if(!(i==0 && j==0)&&((x+i)in 0 until rows)&& ((y+j)in 0 until columns))
                            reveal(x+i, y+j)
                    }
                }
            }
        }
    }
    private fun gameLost(){
        revealAllMines()
        disableAllButton()
        running=false
        Toast.makeText(this, "You Loose. Keep trying ", Toast.LENGTH_LONG).show()
        updateScore("Lost")
    }
    private fun revealAllMines(){
        for(i in 0 until rows){
            for(j in 0 until columns){
                if(board[i][j].value==MINE){
                    val button=getButton(i, j)
                    button.setBackgroundResource(R.drawable.mine)
                }
            }
        }
    }
    private fun disableAllButton(){
        for(x in 0 until rows){
            for(y in 0 until columns){
                val button =getButton(x, y)
                button.isEnabled=false
                button.setTextColor(ContextCompat.getColor(this, R.color.purple_700))
            }
        }
    }
    private fun updateScore(status:String){
        val sharedPreference=getSharedPreferences("Scores", Context.MODE_PRIVATE)
        var bestTime=sharedPreference.getInt("BEST_TIME", 0)
        if(status=="won"){
            if(bestTime==0){
                bestTime=second
            }
        else{
            if(second<bestTime)
                bestTime=second
        }
    }
    val sharedPreferencesUpdate=getSharedPreferences("Scores", Context.MODE_PRIVATE)
    with(sharedPreferencesUpdate.edit()){
        putInt("BEST_TIME", bestTime)
        putInt("LAST_GAME_TIME", second)
        commit()
    }
    }
    private fun isComplete():Boolean{
        var minesMarked=true
        board.forEach {
            row->row.forEach {
                if(it.value==MINE){
                    if(!it.isMarked)
                        minesMarked=false
                }
        }
        }
        var valuesReveales=true
        board.forEach { row->
            row.forEach {
                if(it.value!=MINE){
                    if(!it.isRevealed){
                        valuesReveales=false
                    }
                }
            }
        }
        return minesMarked||valuesReveales
    }
    private fun showDialog(){
        val builder=AlertDialog.Builder(this)
        with(builder){
            setTitle("Share you win")
            setMessage("Let other know you great winn , Do you wish you score with your friends?")
            setPositiveButton("Yes"){
                    _, _ ->val intent=Intent(Intent.ACTION_SEND);
                val body="Hello, I won the great Minesweeper game. I finished is $second seconds."
                intent.type="text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "I won !")
                intent.putExtra(Intent.EXTRA_TEXT , body)
                startActivity(Intent.createChooser(intent, "Share you win on ..."))
            }
            setNegativeButton(
                "No"
            ){ _, _ ->}
            val alertDialog=builder.create()
            alertDialog.show()
        }
    }
    private fun restartGame(){
        count =0
        running=false
        second=0
        flagCount= mines
        flag=false
        for(x in 0 until rows){
            for(y in 0 until columns){
                board[x][y].value=0
                board[x][y].isMarked=false
                board[x][y].isRevealed=false
                val button =getButton(x, y)
                button.text=""
                button.setBackgroundResource(R.drawable.button)
            }
            }
        tvmenis.text="$flagCount"
        imgflag.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.button))
        }

    override fun onResume() {
        super.onResume()
        if(wasRunning)
            running=true
    }

    override fun onPause() {
        super.onPause()
        wasRunning=running
        running=false
    }
    }
