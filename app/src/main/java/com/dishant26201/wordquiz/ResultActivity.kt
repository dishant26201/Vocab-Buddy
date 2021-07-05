package com.dishant26201.wordquiz

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivityResultBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type


private const val TAG = "ResultActivity"
private const val BASE_URL = "https://api.twinword.com/"
private const val API_KEY = "demo" // This a dummy key. The API will not work with this key

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val testType = intent.getStringExtra(Constants.TEST_TYPE)!!

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

        if (getArrayList("scoreList") == null){
            val scoreListNew = ArrayList<ScoreDataClasses?>() //Creating an empty arraylist
            val scoreDataEntry = ScoreDataClasses(intent.getStringExtra(Constants.TEST_TYPE)!!, intent.getStringExtra(Constants.DIFFICULTY)!!, correctAnswers, totalQuestions)
            scoreListNew.add(scoreDataEntry)
            saveArrayList(scoreListNew, "scoreList")
        }
        else {
            val scoreList = getArrayList("scoreList")
            val scoreDataEntry = ScoreDataClasses(intent.getStringExtra(Constants.TEST_TYPE)!!, intent.getStringExtra(Constants.DIFFICULTY)!!, correctAnswers, totalQuestions)
            scoreList!!.add(scoreDataEntry)
            saveArrayList(scoreList, "scoreList")
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

        binding.btnScoreCard.setOnClickListener {
            val intent = Intent(this, ScoreCardActivity::class.java)
            intent.putExtra(Constants.TEST_TYPE, testType)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
    }

    fun saveArrayList(list: ArrayList<ScoreDataClasses?>?, key: String?) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply() // This line is IMPORTANT !!!
    }

    fun getArrayList(key: String?): ArrayList<ScoreDataClasses?>? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json = prefs.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<ScoreDataClasses?>?>() {}.type
        return gson.fromJson(json, type)
    }
}
