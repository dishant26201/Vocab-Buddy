package com.dishant26201.wordquiz

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivityQuizQuestionsBinding
import com.dishant26201.wordquiz.databinding.ActivityResultBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "ResultActivity"
private const val BASE_URL = "https://api.twinword.com/"
private const val API_KEY = "ejxv9wESxNQ5XFJpS0KQpfUd36rzGcQXEmWfq0QQ+OmxiZ3S8LY1wSYY4AH5EqY9UWx2KxAgRNbZ9N6SUBsNqw=="

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val correctAnswers = intent.getIntExtra(Constants.CORRECT_ANSWERS, 0)
        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTION, 0)
        binding.tvScore.text = "Your Score is $correctAnswers out of $totalQuestions"

        if (correctAnswers >= 0 && correctAnswers <= 5) {
            binding.tvInitialComment.text = "Hmmm..."
            binding.ivPerformanceImage.setImageResource(R.drawable.ic_dont_worry)
            binding.tvFeedback.text = "Don't worry. Some polishing of skills is all you need!"
        }
        else if (correctAnswers > 5 && correctAnswers <= 7) {
            binding.tvInitialComment.text = "Good Job!"
            binding.ivPerformanceImage.setImageResource(R.drawable.ic_thumbs_up)
            binding.tvFeedback.text = "You're getting there! Don't stop the practice!"
        }
        else if (correctAnswers > 7 && correctAnswers <= 10) {
            binding.tvInitialComment.text = "Way To Go!"
            binding.ivPerformanceImage.setImageResource(R.drawable.ic_trophy)
            binding.tvFeedback.text = "This is fantastic! But don't get overconfident. Keep practicing!"
        }


        binding.btnFinish.setOnClickListener {
            Constants.questionsListX.clear()
            startActivity(Intent(this, LaunchActivity::class.java))
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnAgain.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to restart this quiz with the same settings?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    val difficulty = intent.getStringExtra(Constants.DIFFICULTY)!!
                    val testType = intent.getStringExtra(Constants.TEST_TYPE)!!
                    Toast.makeText(
                        this@ResultActivity,
                        "Loading questions. Please wait.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnAgain.isClickable = false
                    binding.btnAgain.isEnabled = false

                    Constants.questionsListX.clear()

                    val retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()


                    val twinwordService = retrofit.create(TwinwordService::class.java)
                    twinwordService.getQuestions(
                        "X-Twaip-Key: $API_KEY",
                        difficulty,
                        testType
                    ).enqueue(object : Callback<QuizAndParams> {
                        override fun onResponse(
                            call: Call<QuizAndParams>,
                            response: Response<QuizAndParams>
                        ) {
                            Log.i(TAG, "onResponse $response")
                            Log.i(TAG, "onResponse ${response.message()}")
                            Log.i(TAG, "onResponse ${response.raw().request().url()}")

                            if (response.code() == 503) {
                                val dialogBuilder = android.app.AlertDialog.Builder(this@ResultActivity)
                                dialogBuilder.setMessage("Sorry. Our servers are currently down. Please try again later.")
                                dialogBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, whichButton -> })
                                val dialog = dialogBuilder.create()
                                dialog.show()
                            }
                            else {
                                val questions = response.body()?.quizlist
                                if (questions != null) {
                                    for (question in questions) {
                                        Constants.setQuestions(question)
                                    }
                                    Log.i(TAG, Constants.getQuestions().toString())
                                    val intent =
                                        Intent(
                                            this@ResultActivity,
                                            QuizQuestionsActivity::class.java
                                        )
                                    intent.putExtra(Constants.TEST_TYPE, testType)
                                    intent.putExtra(Constants.DIFFICULTY, difficulty)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }

                        override fun onFailure(call: Call<QuizAndParams>, t: Throwable) {
                            Log.i(TAG, "onFailure $t")
                            Toast.makeText(
                                this@ResultActivity,
                                "API not called. Check internet.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
                .setNegativeButton("No") { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }
}