package com.dishant26201.wordquiz

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.graphics.drawable.VectorDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.dishant26201.quizapp.Constants
import com.dishant26201.test.DictionaryService
import com.dishant26201.test.WordResults
import com.dishant26201.wordquiz.R.color.disableGrey
import com.dishant26201.wordquiz.R.color.disableGreyNight
import com.dishant26201.wordquiz.databinding.ActivityQuizQuestionsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


private const val TAG = "QuizQuestionsActivity"
private const val BASE_URL = "https://api.twinword.com/"
private const val BASE_URL2 = "https://api.dictionaryapi.dev/api/v2/"

class QuizQuestionsActivity : AppCompatActivity(), View.OnClickListener {

    private var mCurrentPosition: Int = 1
    private var mQuestionsList: ArrayList<Questions>? = null
    private var mSelectedOptionPosition: Int = 0
    private var mCorrectAnswers: Int = 0
    private var submitted = false
    private var opSelected = false

    private var darkMode : Boolean = false

    private lateinit var restart : MenuItem
    private lateinit var exit : MenuItem

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
                darkMode = true
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
                darkMode = false
            }
        }

        mQuestionsList = Constants.getQuestions()

        setQuestion()

        binding.tvQuestion1.setOnClickListener(this)
        binding.tvQuestion2.setOnClickListener(this)
        binding.tvQuestion3.setOnClickListener(this)
        binding.tvOp1.setOnClickListener(this)
        binding.tvOp2.setOnClickListener(this)
        binding.btnCheck.setOnClickListener(this)


    }

    // inflate app bar with menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        restart = menu!!.findItem(R.id.ic_restart)
        exit = menu!!.findItem(R.id.ic_exit)
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

                        Toast.makeText(
                            this@QuizQuestionsActivity,
                            "Loading questions. Please wait.",
                            Toast.LENGTH_SHORT
                        ).show()

                        item.isEnabled = false

                        val difficulty = intent.getStringExtra(Constants.DIFFICULTY)!!
                        val testType = intent.getStringExtra(Constants.TEST_TYPE)!!

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
                                    val dialogBuilder = android.app.AlertDialog.Builder(this@QuizQuestionsActivity)
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
                                        val intent = Intent(
                                            this@QuizQuestionsActivity,
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
                                    this@QuizQuestionsActivity,
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
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvQuestion1 -> {
                if (submitted) {

                    exit.isEnabled = false
                    restart.isEnabled = false


                    binding.tvQuestion1.isClickable = false
                    binding.tvQuestion2.isClickable = false
                    binding.tvQuestion3.isClickable = false
                    binding.tvOp1.isClickable = false
                    binding.tvOp2.isClickable = false
                    binding.btnCheck.isClickable = false

                    val word = binding.tvQuestion1.text.toString()
                    findMeaning(word)
                }
                else {
                    Toast.makeText(this, "Answer the question to view the meaning of this word", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.tvQuestion2 -> {
                if (submitted) {

                    exit.isEnabled = false
                    restart.isEnabled = false

                    binding.tvQuestion1.isClickable = false
                    binding.tvQuestion2.isClickable = false
                    binding.tvQuestion3.isClickable = false
                    binding.tvOp1.isClickable = false
                    binding.tvOp2.isClickable = false
                    binding.btnCheck.isClickable = false

                    val word = binding.tvQuestion2.text.toString()
                    findMeaning(word)
                }
                else {
                    Toast.makeText(this, "Answer the question to view the meaning of this word", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.tvQuestion3 -> {
                if (submitted) {

                    exit.isEnabled = false
                    restart.isEnabled = false

                    binding.tvQuestion1.isClickable = false
                    binding.tvQuestion2.isClickable = false
                    binding.tvQuestion3.isClickable = false
                    binding.tvOp1.isClickable = false
                    binding.tvOp2.isClickable = false
                    binding.btnCheck.isClickable = false

                    val word = binding.tvQuestion3.text.toString()
                    findMeaning(word)
                }
                else {
                    Toast.makeText(this, "Answer the question to view the meaning of this word", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.tvOp1 -> {
                if (!submitted) {
                    selectedOptionLook(binding.tvOp1, 1)
                    opSelected = true
                }
                else {
                    exit.isEnabled = false
                    restart.isEnabled = false
                    binding.tvQuestion1.isClickable = false
                    binding.tvQuestion2.isClickable = false
                    binding.tvQuestion3.isClickable = false
                    binding.tvOp1.isClickable = false
                    binding.tvOp2.isClickable = false
                    binding.btnCheck.isClickable = false

                    val word = binding.tvOp1.text.toString()
                    findMeaning(word)
                }
            }
            R.id.tvOp2 -> {
                if (!submitted) {
                    selectedOptionLook(binding.tvOp2, 2)
                    opSelected = true
                }
                else {
                    exit.isEnabled = false
                    restart.isEnabled = false
                    binding.tvQuestion1.isClickable = false
                    binding.tvQuestion2.isClickable = false
                    binding.tvQuestion3.isClickable = false
                    binding.tvOp1.isClickable = false
                    binding.tvOp2.isClickable = false
                    binding.btnCheck.isClickable = false

                    val word = binding.tvOp2.text.toString()
                    findMeaning(word)
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
                                val difficulty = intent.getStringExtra(Constants.DIFFICULTY)!!
                                val testType = intent.getStringExtra(Constants.TEST_TYPE)!!
                                val intent = Intent(this, ResultActivity::class.java)
                                intent.putExtra(Constants.CORRECT_ANSWERS, mCorrectAnswers)
                                intent.putExtra(Constants.TOTAL_QUESTION, mQuestionsList!!.size)
                                intent.putExtra(Constants.TEST_TYPE, testType)
                                intent.putExtra(Constants.DIFFICULTY, difficulty)
                                startActivity(intent)
                                finish()
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

    private fun setQuestion(){
        val question = mQuestionsList!![mCurrentPosition - 1]

        binding.tvTap.setTypeface(null, Typeface.BOLD)

        if (darkMode) {
            binding.ivLockUnlock.setImageResource(R.drawable.ic_bulb_disable_night)
            binding.tvTap.setTextColor(ContextCompat.getColor(this, disableGreyNight))
        } else {
            binding.ivLockUnlock.setImageResource(R.drawable.ic_bulb_disable)
            binding.tvTap.setTextColor(ContextCompat.getColor(this, disableGrey))
        }

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
        binding.tvTap.setTextColor(AppCompatResources.getColorStateList(this, R.color.darkGrassGreen))
        binding.tvTap.setTypeface(null, Typeface.BOLD)
        binding.ivLockUnlock.setImageResource(R.drawable.ic_bulb)

        when (answer) {
            1 -> {
                binding.tvOp1.background = ContextCompat.getDrawable(this, drawableView)
            }
            2 -> {
                binding.tvOp2.background = ContextCompat.getDrawable(this, drawableView)
            }
        }
    }

    private fun findMeaning(word: String) {

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val dictionaryService = retrofit.create(DictionaryService::class.java)


        val popUpView : View? = layoutInflater.inflate(R.layout.custom_popup, null, false)
        popUpView!!.findViewById<TextView>(R.id.tvWord).text = word.toString()
            .lowercase().replaceFirstChar { it.uppercase() }

        dictionaryService.getMeaning(word).enqueue(object : Callback<List<WordResults>> {
//            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onResponse(
                call: Call<List<WordResults>>,
                response: Response<List<WordResults>>
            ) {

                Log.i(TAG, "onResponse $response")

                if (response.code() == 200 && !(response.body()!![0].meanings.isNullOrEmpty())){
                    val meanings = response.body()!![0].meanings
                    val audioUrl = response.body()!![0].phonetics[0].audio


                    for (meaning in meanings) {

                        val meaningView : View

                        if (!meaning.definitions[0].synonyms.isNullOrEmpty()) {
                            meaningView = layoutInflater.inflate(R.layout.meaning_layout_synonym, null, false)
                            meaningView.findViewById<TextView>(R.id.tvMeaningHeading).text = meaning.partOfSpeech
                            meaningView.findViewById<TextView>(R.id.tvMeaningHeading).setTypeface(null, Typeface.BOLD_ITALIC)
                            meaningView.findViewById<TextView>(R.id.tvMeaning).text = meaning.definitions[0].definition
                            val separator = ",  "
                            val textSimilarWords = meaning.definitions[0].synonyms!!.joinToString(separator)
                            meaningView.findViewById<TextView>(R.id.tvSynonyms).text = textSimilarWords.toString()
                            popUpView.findViewById<LinearLayout>(R.id.llMeaningHolder).addView(meaningView)
                        }
                        else {
                            meaningView = layoutInflater.inflate(R.layout.meaning_layout, null, false)

                            meaningView.findViewById<TextView>(R.id.tvMeaningHeading).text = meaning.partOfSpeech
                            meaningView.findViewById<TextView>(R.id.tvMeaningHeading).setTypeface(null, Typeface.BOLD_ITALIC)
                            meaningView.findViewById<TextView>(R.id.tvMeaning).text = meaning.definitions[0].definition

                            popUpView.findViewById<LinearLayout>(R.id.llMeaningHolder).addView(meaningView)
                        }


                        Log.i(TAG, "${meaningView.findViewById<TextView>(R.id.tvMeaningHeading).text}" )
                        Log.i(TAG, "${meaningView.findViewById<TextView>(R.id.tvMeaning).text}" )

                    }
                    createPopUp(popUpView, audioUrl)
                }
                else {
                    val audioUrl = null
                    val noMeaningView : View = layoutInflater.inflate(R.layout.no_meaning, null, false)

                    popUpView.findViewById<LinearLayout>(R.id.llMeaningHolder).addView(noMeaningView)

                    createPopUp(popUpView, audioUrl)

                    Toast.makeText(this@QuizQuestionsActivity, "Could not find meaning", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<List<WordResults>>, t: Throwable) {

                val audioUrl = null

                val noMeaningView : View = layoutInflater.inflate(R.layout.no_meaning, null, false)

                popUpView.findViewById<LinearLayout>(R.id.llMeaningHolder).addView(noMeaningView)

                createPopUp(popUpView, audioUrl)

                Toast.makeText(this@QuizQuestionsActivity, "Could not find meaning", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun createPopUp(popUpView : View?, audioUrl : String?) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(popUpView)
            .create()
        popUpView!!.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            dialog.dismiss()
        }

        if (audioUrl != null) {
            popUpView.findViewById<ImageButton>(R.id.btnAudio)?.setOnClickListener {
                Toast.makeText(this, "Fetching audio...", Toast.LENGTH_SHORT).show()

                val mediaPlayer = MediaPlayer()

                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                try {
                    mediaPlayer.setDataSource(audioUrl)
                    // below line is use to prepare
                    // and start our media player.
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // below line is use to display a toast message.
                // below line is use to display a toast message.
            }
        }
        else {
            popUpView.findViewById<ImageButton>(R.id.btnAudio)?.isClickable = false
            popUpView.findViewById<ImageButton>(R.id.btnAudio)?.isEnabled = false
        }
        dialog.show()
        binding.tvQuestion1.isClickable = true
        binding.tvQuestion2.isClickable = true
        binding.tvQuestion3.isClickable = true
        binding.tvOp1.isClickable = true
        binding.tvOp2.isClickable = true
        binding.btnCheck.isClickable = true
        exit.isEnabled = true
        restart.isEnabled = true
    }
}
