package com.dishant26201.wordquiz

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.dishant26201.test.DictionaryService
import com.dishant26201.test.ShortDef
import com.dishant26201.test.ShortDefService
import com.dishant26201.test.WordResults
import com.dishant26201.wordquiz.databinding.ActivityDifficultWordsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


private const val TAG = "DifficultWordAcctivity"
private const val DICTIONARY_URL = "https://api.dictionaryapi.dev/api/v2/"
private const val ALTERNATE_DICTIONARY_URL = "https://dictionaryapi.com/api/v3/references/collegiate/json/"

class DifficultWordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDifficultWordsBinding
    var selectedItemKey = "a - z"

    private var flag = true

    private val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDifficultWordsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        params.setMargins(0, 24, 0, 24)

        supportActionBar?.title = "Saved Words"

        // showing the back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primaryColor))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
            }
        }

        binding.autoCompleteTextView4.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectedItemKey = parent.getItemAtPosition(position).toString().lowercase()
            binding.llWordStore.removeAllViews()
            if (!getArrayListMeaning("general").isNullOrEmpty()) {
                binding.tvTapWord.text = "Tap on a word for the definition!"
                var meaningList = getArrayListMeaning("general")!!.sortedWith(compareBy { it })
                when (selectedItemKey) {
                    "a - z" -> {
                        meaningList = meaningList.sortedWith(compareBy { it })
                    }
                    "z - a" -> {
                        meaningList = meaningList.sortedWith(compareBy { it })
                        meaningList = meaningList.reversed()
                    }
                }

                for (meaning in meaningList) {
                    val meaningRow: View = layoutInflater.inflate(R.layout.word_row_layout, null, false)
                    meaningRow.findViewById<TextView>(R.id.tvWordItem).text = meaning!!.toString().uppercase()
                    meaningRow.setLayoutParams(params)
                    binding.llWordStore.addView(meaningRow)
                    meaningRow.setOnClickListener {
                        flag = false
                        findMeaningDictionary(meaning.toString().lowercase())
                    }
                }
            }
            else {
                binding.tvTapWord.text = "Saved words appear below"
            }
        }


        binding.llWordStore.removeAllViews()
        if (!getArrayListMeaning("general").isNullOrEmpty()) {
            binding.tvTapWord.text = "Tap on a word for the definition!"
            var meaningList = getArrayListMeaning("general")!!.sortedWith(compareBy { it })
            when (selectedItemKey) {
                "a - z" -> {
                    meaningList = meaningList.sortedWith(compareBy { it })
                }
                "z - a" -> {
                    meaningList = meaningList.sortedWith(compareBy { it })
                    meaningList = meaningList.reversed()
                }
            }

            for (meaning in meaningList) {
                val meaningRow: View = layoutInflater.inflate(R.layout.word_row_layout, null, false)
                meaningRow.findViewById<TextView>(R.id.tvWordItem).text = meaning!!.toString().uppercase()
                meaningRow.setLayoutParams(params)
                binding.llWordStore.addView(meaningRow)
                meaningRow.setOnClickListener {
                    flag = false
                    findMeaningDictionary(meaning.toString().lowercase())
                }
            }
        }
        else {
            binding.tvTapWord.text = "Saved words appear below"
        }
    }

    override fun onResume() {
        super.onResume()

        val scoreFilters = resources.getStringArray(R.array.sortBy)
        val arrayAdapter = ArrayAdapter(this , R.layout.dropdown_item, scoreFilters)
        binding.autoCompleteTextView4.setAdapter(arrayAdapter)
    }

    // this event will enable the back
    // function to the button on press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (flag) {
                    finish()
                }
                else {
                    Log.i(TAG, "Back button no response")
                }
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

    private fun alternateDef(word: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(ALTERNATE_DICTIONARY_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val shortDefService = retrofit.create(ShortDefService::class.java)

        val popUpView : View? = layoutInflater.inflate(R.layout.custom_popup, null, false)
        popUpView!!.findViewById<TextView>(R.id.tvWord).text = word.toString()
            .lowercase().replaceFirstChar { it.uppercase() }

        shortDefService.getShortDef(word).enqueue(object : Callback<List<ShortDef>> {
            override fun onResponse(call: Call<List<ShortDef>>, response: Response<List<ShortDef>>) {
                Log.i(TAG, "onResponse $response")

                if (response.code() == 200 && !(response.body()!!.isNullOrEmpty()) && !(response.body()!![0].shortdef.isNullOrEmpty())){
                    val audioUrl = null
                    val meanings = response.body()!![0].shortdef
                    var cnt = 1
                    for (meaning in meanings) {
                        val meaningView : View

                        meaningView = layoutInflater.inflate(R.layout.meaning_layout, null, false)

                        meaningView.findViewById<TextView>(R.id.tvMeaningHeading).text = "definition " + cnt
                        meaningView.findViewById<TextView>(R.id.tvMeaningHeading).setTypeface(null, Typeface.BOLD_ITALIC)
                        meaningView.findViewById<TextView>(R.id.tvMeaning).text = meaning

                        popUpView.findViewById<LinearLayout>(R.id.llMeaningHolder).addView(meaningView)


                        Log.i(TAG, "${meaningView.findViewById<TextView>(R.id.tvMeaningHeading).text}" )
                        Log.i(TAG, "${meaningView.findViewById<TextView>(R.id.tvMeaning).text}" )
                        cnt ++
                    }
                    createPopUpDictionary(popUpView, audioUrl, word, true)
                }
                else {
                    val audioUrl = null
                    val noMeaningView : View = layoutInflater.inflate(R.layout.no_meaning, null, false)

                    popUpView.findViewById<LinearLayout>(R.id.llMeaningHolder).addView(noMeaningView)

                    createPopUpDictionary(popUpView, audioUrl, word, false)
                }
                flag = true

            }

            override fun onFailure(call: Call<List<ShortDef>>, t: Throwable) {
                val audioUrl = null

                val noMeaningView : View = layoutInflater.inflate(R.layout.no_meaning, null, false)

                popUpView.findViewById<LinearLayout>(R.id.llMeaningHolder).addView(noMeaningView)

                createPopUpDictionary(popUpView, audioUrl, word, false)
                flag = true
                Log.i(TAG, t.toString())
            }
        })
    }


    private fun findMeaningDictionary(word: String) {
        flag = false

        val retrofit = Retrofit.Builder()
            .baseUrl(DICTIONARY_URL)
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

                if (response.code() == 200 && !(response.body()!!.isNullOrEmpty()) && !(response.body()!![0].meanings.isNullOrEmpty())){
                    val audioUrl : String?
                    val meanings = response.body()!![0].meanings
                    if (response.body()!![0].phonetics.isNullOrEmpty()){
                        audioUrl = null
                    }
                    else {
                        audioUrl = response.body()!![0].phonetics[0].audio
                    }

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
                    createPopUpDictionary(popUpView, audioUrl, word, true)
                }
                else {
                    Toast.makeText(this@DifficultWordsActivity, "Please wait...", Toast.LENGTH_SHORT).show()
                    alternateDef(word)
                }
            }

            override fun onFailure(call: Call<List<WordResults>>, t: Throwable) {

                Toast.makeText(this@DifficultWordsActivity, "Please wait...", Toast.LENGTH_SHORT).show()
                alternateDef(word)
            }

        })
    }

    private fun createPopUpDictionary(popUpView : View?, audioUrl : String?, wordX: String, meaningYes : Boolean) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(popUpView)
            .create()
        popUpView!!.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            dialog.dismiss()
        }

        if (getArrayListMeaning("general") != null){
            val meaningList = getArrayListMeaning("general")
            var cnt = 0
            for (meaning in meaningList!!) {
                if (wordX.lowercase() == meaning!!.toString().lowercase()) {
                    cnt ++
                }
            }
            if (cnt == 0) {
                popUpView.findViewById<ImageButton>(R.id.btnBookmarkDiffWord).setImageResource(R.drawable.ic_bookmark_not_added)

            }
            else {
                popUpView.findViewById<ImageButton>(R.id.btnBookmarkDiffWord).setImageResource(R.drawable.ic_bookmark_added)
            }
        }
        else {
            popUpView.findViewById<ImageButton>(R.id.btnBookmarkDiffWord).setImageResource(R.drawable.ic_bookmark_not_added)
        }

        popUpView.findViewById<ImageButton>(R.id.btnBookmarkDiffWord).setOnClickListener {
            if (meaningYes) {
                if (getArrayListMeaning("general") == null){
                    val meaningListNew = ArrayList<String?>() //Creating an empty arraylist

                    val entry = wordX.lowercase()

                    meaningListNew.add(entry)
                    saveArrayListMeaning(meaningListNew, "general")
                    popUpView.findViewById<ImageButton>(R.id.btnBookmarkDiffWord).setImageResource(R.drawable.ic_bookmark_added)
                    Toast.makeText(this, "Word added to saved", Toast.LENGTH_SHORT).show()
                }
                else {
                    val meaningList = getArrayListMeaning("general")
                    val removeList = ArrayList<String?>()
                    var cnt = 0
                    for (meaning in meaningList!!) {
                        if (wordX.lowercase() == meaning!!.toString().lowercase()) {
                            removeList.add(meaning.toString().lowercase())
                            cnt ++
                        }
                    }
                    if (cnt > 0) {
                        meaningList.removeAll(removeList)
                        popUpView.findViewById<ImageButton>(R.id.btnBookmarkDiffWord).setImageResource(R.drawable.ic_bookmark_not_added)
                        Toast.makeText(this, "Word removed from saved", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val entry = wordX.lowercase()
                        meaningList.add(entry)
                        popUpView.findViewById<ImageButton>(R.id.btnBookmarkDiffWord).setImageResource(R.drawable.ic_bookmark_added)
                        Toast.makeText(this, "Word added to saved", Toast.LENGTH_SHORT).show()
                    }
                    saveArrayListMeaning(meaningList, "general")
                    binding.llWordStore.removeAllViews()
                    if (!getArrayListMeaning("general").isNullOrEmpty()) {

                        binding.tvTapWord.text = "Tap on a word for the definition!"

                        var meaningList = getArrayListMeaning("general")!!.sortedWith(compareBy { it })
                        when (selectedItemKey) {
                            "a - z" -> {
                                meaningList = meaningList.sortedWith(compareBy { it })
                            }
                            "z - a" -> {
                                meaningList = meaningList.sortedWith(compareBy { it })
                                meaningList = meaningList.reversed()
                            }
                        }

                        for (meaning in meaningList) {
                            val meaningRow: View = layoutInflater.inflate(R.layout.word_row_layout, null, false)
                            meaningRow.findViewById<TextView>(R.id.tvWordItem).text = meaning!!.toString().uppercase()
                            params.setMargins(0, 24, 0, 24)
                            meaningRow.setLayoutParams(params)
                            binding.llWordStore.addView(meaningRow)
                            meaningRow.setOnClickListener {
                                findMeaningDictionary(meaning.toString().lowercase())
                            }
                        }
                    }
                    else {
                        binding.tvTapWord.text = "Saved words appear below"
                    }
                }
            }
            else {
                Toast.makeText(this, "Sorry, you can't save this word", Toast.LENGTH_SHORT)
                    .show()
            }

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
            }
        }
        else {
            popUpView.findViewById<ImageButton>(R.id.btnAudio)?.isClickable = false
            popUpView.findViewById<ImageButton>(R.id.btnAudio)?.isEnabled = false
        }
        dialog.show()
        flag = true
    }

    fun saveArrayListMeaning(list: ArrayList<String?>?, key: String?) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        val gson = Gson()

        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply() // This line is IMPORTANT !!!
    }

    fun getArrayListMeaning(key: String?): ArrayList<String?>? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json = prefs.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return gson.fromJson(json, type)
    }
}