package com.dishant26201.quizapp

import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import com.dishant26201.wordquiz.*

private const val TAG = "API_CALL"
object Constants {

    private const val BASE_URL = "https://api.twinword.com/"
    private const val API_KEY = "ejxv9wESxNQ5XFJpS0KQpfUd36rzGcQXEmWfq0QQ+OmxiZ3S8LY1wSYY4AH5EqY9UWx2KxAgRNbZ9N6SUBsNqw=="

    const val CORRECT_ANSWERS: String = "correct_answers"
    const val TOTAL_QUESTION: String = "total_questions"

    val questionsListX = ArrayList<Questions>()

    fun setQuestions(item: Questions) {
        questionsListX.add(item)
    }

    fun getQuestions(): ArrayList<Questions> {
        questionsListX.shuffle()
        return questionsListX
    }

    fun setDisplayMode(isChecked: Boolean) {
        if (isChecked == false) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
// END