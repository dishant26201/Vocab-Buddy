package com.dishant26201.wordquiz

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivityQuizQuestionsBinding

private const val TAG = "QuizQuestionsActivity"
private const val BASE_URL = "https://api.twinword.com/"
private const val API_KEY = "ejxv9wESxNQ5XFJpS0KQpfUd36rzGcQXEmWfq0QQ+OmxiZ3S8LY1wSYY4AH5EqY9UWx2KxAgRNbZ9N6SUBsNqw=="

class QuizQuestionsActivity : AppCompatActivity(), View.OnClickListener {

    private var mCurrentPosition: Int = 1
    private var mQuestionsList: ArrayList<Questions>? = null
    private var mSelectedOptionPosition: Int = 0
    private var mCorrectAnswers: Int = 0
    private var submitted = false
    private var opSelected = false

    private lateinit var binding: ActivityQuizQuestionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizQuestionsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = "Vocab Buddy"

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primaryColor))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
            }
        }

        mQuestionsList = Constants.getQuestions()

        setQuestion()

        binding.tvOp1.setOnClickListener(this)
        binding.tvOp2.setOnClickListener(this)

        binding.btnCheck.setOnClickListener(this)

    }

    // inflate app bar with menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ic_exit -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to exit this quiz? You will lose all your progress.")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        // Go back to LaunchActivity
                        Constants.questionsListX.clear()
                        val intent = Intent(this, LaunchActivity::class.java)
                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                    .setNegativeButton("No") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
            R.id.ic_restart -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to restart this quiz? You will lose all your progress.")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        // Clear questionsListX, call API again, and reload activity
                        val intent = intent
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("No") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setQuestion(){
        val question = mQuestionsList!![mCurrentPosition - 1]

        defaultOptionLook()

        if (mCurrentPosition == mQuestionsList!!.size + 1) {
            binding.btnCheck.text = "FINISH"
        } else {
            binding.btnCheck.text = "SUBMIT"
        }

        binding.progressBar.progress = mCurrentPosition
        binding.tvProgress.text = "$mCurrentPosition/${binding.progressBar.max}"


        binding.tvQuestion1.text = question.quiz[0].uppercase()
        binding.tvQuestion2.text = question.quiz[1].uppercase()
        binding.tvQuestion3.text = question.quiz[2].uppercase()

        binding.tvOp1.text = question.option[0].uppercase()
        binding.tvOp2.text = question.option[1].uppercase()


        submitted = false
        opSelected = false
    }


    // default look of an answer option
    private fun defaultOptionLook() {
        val options = ArrayList<TextView>()
        options.add(0, binding.tvOp1)
        options.add(1, binding.tvOp2)

        for (option in options) {
            option.setTextColor(Color.parseColor("#666666"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(this, R.drawable.default_option_bg)
        }
    }

    // look of an answer option when clicked/selected
    private fun selectedOptionLook(tv: TextView, selectedOptionNumber: Int) {

        defaultOptionLook()
        mSelectedOptionPosition = selectedOptionNumber

        tv.setTextColor(Color.parseColor("#363A43"))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.background = ContextCompat.getDrawable(this, R.drawable.selected_option_border)

    }

    private fun answerLook(answer: Int, drawableView: Int) {
        when (answer) {
            1 -> {
                binding.tvOp1.background = ContextCompat.getDrawable(this, drawableView)
            }
            2 -> {
                binding.tvOp2.background = ContextCompat.getDrawable(this, drawableView)
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvOp1 -> {
                if (submitted == false) {
                    selectedOptionLook(binding.tvOp1, 1)
                    opSelected = true
                }
            }
            R.id.tvOp2 -> {
                if (submitted == false) {
                    selectedOptionLook(binding.tvOp2, 2)
                    opSelected = true
                }
            }
            R.id.btnCheck -> {
                if (opSelected == false){
                    Toast.makeText(this, "Please select an option before proceeding."+"\n"+"If you're unsure take a guess.", Toast.LENGTH_SHORT).show()
                }
                else {
                    submitted = true
                    if (mSelectedOptionPosition == 0) {
                        mCurrentPosition++
                        when {
                            mCurrentPosition <= mQuestionsList!!.size -> {
                                setQuestion()
                            }
                            else -> {
                                val intent = Intent(this, ResultActivity::class.java)
                                intent.putExtra(Constants.CORRECT_ANSWERS, mCorrectAnswers)
                                intent.putExtra(Constants.TOTAL_QUESTION, mQuestionsList!!.size)
                                startActivity(intent)
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                finish()
                            }
                        }
                    } else if (mSelectedOptionPosition != 0) {
                        val question = mQuestionsList?.get(mCurrentPosition - 1)

                        if (question != null) {
                            if (question.correct != mSelectedOptionPosition) {
                                answerLook(mSelectedOptionPosition, R.drawable.incorrect_option_bg)
                            } else {
                                mCorrectAnswers ++
                            }
                        }
                        if (question != null) {
                            answerLook(question.correct, R.drawable.correct_option_bg)
                        }


                        if (mCurrentPosition == mQuestionsList!!.size) {
                            binding.btnCheck.text = "VIEW RESULTS"
                        } else {
                            binding.btnCheck.text = "NEXT QUESTION"
                        }
                        mSelectedOptionPosition = 0
                    }
                }
            }
        }

    }
}