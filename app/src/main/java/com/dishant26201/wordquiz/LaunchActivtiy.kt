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


private const val TAG = "LaunchActivity"
private const val BASE_URL = "https://api.twinword.com/"
private const val API_KEY = "demo" // this is a dummy key. The API will not work with this key

class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding
    var selectedItem = "sat"

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

        binding.autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
                 selectedItem = parent.getItemAtPosition(position).toString()
        }


        binding.btnStart.setOnClickListener {
            if (binding.etDifficulty.text.toString().isEmpty()){
                Toast.makeText(this, "Please select a difficulty level before proceeding", Toast.LENGTH_SHORT).show()
            }
            else if (binding.etDifficulty.text.toString().toInt() <= 0 || binding.etDifficulty.text.toString().toInt() > 10) {
                Toast.makeText(this, "Please enter a number between 1 and 10", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(
                    this@LaunchActivity,
                    "Loading questions. Please wait.",
                    Toast.LENGTH_SHORT
                ).show()

                binding.btnStart.isEnabled = false
                binding.btnStart.isClickable = false

                val twinwordService = retrofit.create(TwinwordService::class.java)
                twinwordService.getQuestions(
                    "X-Twaip-Key: $API_KEY",
                    binding.etDifficulty.text.toString(),
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
                                    binding.etDifficulty.text.toString()
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
                            "Check internet and restart app",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this@LaunchActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val tests = resources.getStringArray(R.array.tests)
        val arrayAdapter = ArrayAdapter(this , R.layout.dropdown_item, tests)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)
    }

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Select the exam you want to prepare for and the difficulty level of the questions that you wish to attempt. " +
                "There are over 19000 words across 8 different test types and 10 dfficulty levels, so you can rest assured that you will never run out of questions to practice!")
        dialogBuilder.setPositiveButton("Got It!", DialogInterface.OnClickListener { dialog, whichButton -> })
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
