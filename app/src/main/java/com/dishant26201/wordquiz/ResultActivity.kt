package com.dishant26201.wordquiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivityQuizQuestionsBinding
import com.dishant26201.wordquiz.databinding.ActivityResultBinding

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
            binding.tvFeedback.text = "Don't worry. Some polishing of skills is all you need!"
        }
        else if (correctAnswers > 5 && correctAnswers <= 7) {
            binding.tvFeedback.text = "You're getting there! Don't stop the practice!!"
        }
        else if (correctAnswers > 7 && correctAnswers <= 10) {
            binding.tvFeedback.text = "This is fantastic! But don't get overconfident. Keep practicing!"
        }


        binding.btnFinish.setOnClickListener {
            Constants.questionsListX.clear()
            startActivity(Intent(this, LaunchActivity::class.java))
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}