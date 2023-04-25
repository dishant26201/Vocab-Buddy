package com.dishant26201.wordquiz

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivityLaunchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.appcompat.widget.AppCompatSpinner
import android.widget.EditText


private const val TAG = "LaunchActivity"
private const val BASE_URL = "https://api.twinword.com/"

class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding
    var selectedItem = "sat"
    var difficulty = "3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val sharedPref1 = getSharedPreferences("darkMode", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref1.getBoolean("isDarkMode", false)
        Constants.setDisplayMode(isDarkMode)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        binding.tvInstructions.setOnClickListener {
            showDialog()
        }

        binding.autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectedItem = parent.getItemAtPosition(position).toString()
        }

        binding.autoCompleteTextView3.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            difficulty = parent.getItemAtPosition(position).toString()
        }

        binding.btnStart.setOnClickListener {
            Toast.makeText(
                this@LaunchActivity,
                "Loading questions. Please wait.",
                Toast.LENGTH_SHORT
            ).show()

            Constants.questionsListX.clear()

            binding.btnStart.isEnabled = false
            binding.btnStart.isClickable = false

            val twinwordService = retrofit.create(TwinwordService::class.java)
            twinwordService.getQuestions(
                difficulty.toString(),
                selectedItem.lowercase()
            ).enqueue(object : Callback<QuizAndParams> {
                override fun onResponse(
                    call: Call<QuizAndParams>,
                    response: Response<QuizAndParams>
                ) {
                    Log.i(TAG, "onResponse $response")
                    Log.i(TAG, "onResponse ${response.message()}")
                    Log.i(TAG, "onResponse ${response.raw().request().url()}")

                    if (response.code() == 503) {
                        val dialogBuilder = AlertDialog.Builder(this@LaunchActivity)
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
                                Intent(this@LaunchActivity, QuizQuestionsActivity::class.java)
                            intent.putExtra(Constants.TEST_TYPE, selectedItem.lowercase())
                            intent.putExtra(
                                Constants.DIFFICULTY,
                                difficulty.toString()
                            )
                            startActivity(intent)
                            finish()
                            overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                        }
                    }
                }
                override fun onFailure(call: Call<QuizAndParams>, t: Throwable) {
                    Log.i(TAG, "onFailure $t")
                    Toast.makeText(
                        this@LaunchActivity,
                        "No internet available. Check your connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnStart.isEnabled = true
                    binding.btnStart.isClickable = true
                }

            })
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this@LaunchActivity, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

        binding.btnScore.setOnClickListener {
            val intent = Intent(this@LaunchActivity, ScoreCardActivity::class.java)
            intent.putExtra(Constants.TEST_TYPE, "Overall")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnSearch.setOnClickListener {
            val intent = Intent(this@LaunchActivity, DictionaryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

        binding.btnBookmark.setOnClickListener {
            val intent = Intent(this@LaunchActivity, DifficultWordsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

    }

    override fun onResume() {
        super.onResume()
        val tests = resources.getStringArray(R.array.tests)
        val arrayAdapter = ArrayAdapter(this , R.layout.dropdown_item, tests)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)

        val difficultyLevel = resources.getStringArray(R.array.difficulty)
        val arrayAdapter2 = ArrayAdapter(this , R.layout.dropdown_item, difficultyLevel)
        binding.autoCompleteTextView3.setAdapter(arrayAdapter2)
    }

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("- Select the exam you want to prepare for and the difficulty level of the questions that you wish to attempt. \n\n" +
                "- Tap on the icons in the top right corner to manage settings, view your scorecard, access the dictionary feature, and view your saved words.")
        dialogBuilder.setPositiveButton("Got It!", DialogInterface.OnClickListener { dialog, whichButton -> })
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
