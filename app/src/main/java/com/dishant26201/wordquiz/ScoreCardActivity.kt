package com.dishant26201.wordquiz

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivityScoreCardBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class ScoreCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoreCardBinding
    private lateinit var selectedItem : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreCardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // calling the action bar
        val actionBar = getSupportActionBar()

        actionBar!!.title = "Scorecard"

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primaryColor))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
            }
        }


        selectedItem = intent.getStringExtra(Constants.TEST_TYPE).toString()

        if (selectedItem.lowercase() == "overall") {
            binding.autoCompleteTextView2.setText("Overall", false)
        }
        else {
            binding.autoCompleteTextView2.setText(selectedItem.uppercase(), false)
        }

        binding.tlScore.removeViews(1, binding.tlScore.childCount - 1)
        if (!(getArrayList("scoreList") == null)) {
            var cnt : Int = 0
            binding.btnReset.isVisible = true
            binding.trHeading.isVisible = true

            val scoreList = getArrayList("scoreList")!!.reversed()
            if (selectedItem.lowercase() == "overall") {
                for (score in scoreList) {
                    val scoreRow: View = layoutInflater.inflate(R.layout.score_row, null, false)

                    scoreRow.findViewById<TextView>(R.id.tvTt).text = score!!.testType.uppercase()
                    scoreRow.findViewById<TextView>(R.id.tvDiff).text =
                        score.difficulty.toString()
                    scoreRow.findViewById<TextView>(R.id.tvTotal).text =
                        "${score.scored}/${score.outOf}"
                    val percentage = (score.scored * 100) / score.outOf
                    scoreRow.findViewById<TextView>(R.id.tvPercentage).text =
                        percentage.toString() + "%"

                    binding.tlScore.addView(scoreRow)
                    cnt ++
                }
            } else {
                for (score in scoreList) {
                    if (score!!.testType.lowercase() == selectedItem.lowercase()) {
                        val scoreRow: View = layoutInflater.inflate(R.layout.score_row, null, false)

                        scoreRow.findViewById<TextView>(R.id.tvTt).text =
                            score!!.testType.uppercase()
                        scoreRow.findViewById<TextView>(R.id.tvDiff).text =
                            score.difficulty.toString()
                        scoreRow.findViewById<TextView>(R.id.tvTotal).text =
                            "${score.scored}/${score.outOf}"
                        val percentage = (score.scored * 100) / score.outOf
                        scoreRow.findViewById<TextView>(R.id.tvPercentage).text =
                            percentage.toString() + "%"

                        binding.tlScore.addView(scoreRow)
                        cnt ++
                    }
                }
            }
            if (cnt == 0) {
                binding.btnReset.isVisible = false
                binding.trHeading.isVisible = false
                val noAttempt : View = layoutInflater.inflate(R.layout.no_attempts, null, false)
                binding.tlScore.addView(noAttempt)
            }
        }
        else {
            binding.btnReset.isVisible = false
            binding.trHeading.isVisible = false
            val noAttempt : View = layoutInflater.inflate(R.layout.no_attempts, null, false)
            binding.tlScore.addView(noAttempt)
        }

        binding.autoCompleteTextView2.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            var cnt = 0

            selectedItem = parent.getItemAtPosition(position).toString()

            binding.tlScore.removeViews(1, binding.tlScore.childCount - 1)
            if (!(getArrayList("scoreList") == null)) {
                binding.btnReset.isVisible = true
                binding.trHeading.isVisible = true

                val scoreList = getArrayList("scoreList")!!.reversed()
                if (selectedItem.lowercase() == "overall") {
                    for (score in scoreList) {
                        val scoreRow: View = layoutInflater.inflate(R.layout.score_row, null, false)

                        scoreRow.findViewById<TextView>(R.id.tvTt).text = score!!.testType.uppercase()
                        scoreRow.findViewById<TextView>(R.id.tvDiff).text =
                            score.difficulty.toString()
                        scoreRow.findViewById<TextView>(R.id.tvTotal).text =
                            "${score.scored}/${score.outOf}"
                        val percentage = (score.scored * 100) / score.outOf
                        scoreRow.findViewById<TextView>(R.id.tvPercentage).text =
                            percentage.toString() + "%"

                        binding.tlScore.addView(scoreRow)
                        cnt ++
                    }

                }else {
                    for (score in scoreList) {
                        if (score!!.testType.lowercase() == selectedItem.lowercase()) {
                            val scoreRow: View = layoutInflater.inflate(R.layout.score_row, null, false)

                            scoreRow.findViewById<TextView>(R.id.tvTt).text =
                                score!!.testType.uppercase()
                            scoreRow.findViewById<TextView>(R.id.tvDiff).text =
                                score.difficulty.toString()
                            scoreRow.findViewById<TextView>(R.id.tvTotal).text =
                                "${score.scored}/${score.outOf}"
                            val percentage = (score.scored * 100) / score.outOf
                            scoreRow.findViewById<TextView>(R.id.tvPercentage).text =
                                percentage.toString() + "%"

                            binding.tlScore.addView(scoreRow)
                            cnt ++
                        }
                    }
                }
                if (cnt == 0) {
                    binding.btnReset.isVisible = false
                    binding.trHeading.isVisible = false
                    val noAttempt : View = layoutInflater.inflate(R.layout.no_attempts, null, false)
                    binding.tlScore.addView(noAttempt)
                }
            }
            else {
                binding.btnReset.isVisible = false
                binding.trHeading.isVisible = false
                val noAttempt : View = layoutInflater.inflate(R.layout.no_attempts, null, false)
                binding.tlScore.addView(noAttempt)
            }
        }

        binding.btnReset.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            var message: String
            if (selectedItem.lowercase() == "overall"){
                message = "Are you sure you want to clear your score history for all test types? You won't be able to retrieve this data again."
            }
            else {
                message = "Are you sure you want to clear your score history for ${selectedItem}? You won't be able to retrieve this data again."
            }
            builder.setMessage(message.toString())
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->

                    binding.tlScore.removeViews(1, binding.tlScore.childCount - 1)

                    val scoreList = getArrayList("scoreList")!!

                    if (selectedItem.lowercase() == "overall") {
                        scoreList.clear()
                    } else {
                        val removeList = ArrayList<ScoreDataClasses?>()
                        for (score in scoreList) {
                            if (score!!.testType.lowercase() == selectedItem.lowercase()) {
                                removeList.add(score)
                            }
                        }
                        scoreList.removeAll(removeList)
                    }
                    binding.btnReset.isVisible = false
                    binding.trHeading.isVisible = false
                    val noAttempt : View = layoutInflater.inflate(R.layout.no_attempts, null, false)
                    binding.tlScore.addView(noAttempt)
                    saveArrayList(scoreList, "scoreList")

                }
                .setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    override fun onResume() {
        super.onResume()
        val scoreFilters = resources.getStringArray(R.array.scoreFilters)
        val arrayAdapter = ArrayAdapter(this , R.layout.dropdown_item, scoreFilters)
        binding.autoCompleteTextView2.setAdapter(arrayAdapter)
    }

    // this event will enable the back
    // function to the button on press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
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

