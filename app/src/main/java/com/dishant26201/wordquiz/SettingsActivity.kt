package com.dishant26201.wordquiz

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val darkModeIcon : Drawable = AppCompatResources.getDrawable(this, R.drawable.ic_dark_mode)!!
        val feedbackIcon : Drawable = AppCompatResources.getDrawable(this, R.drawable.ic_feedback)!!
        val shareIcon : Drawable = AppCompatResources.getDrawable(this, R.drawable.ic_share)!!
        val rateIcon : Drawable = AppCompatResources.getDrawable(this, R.drawable.ic_like)!!

        supportActionBar?.title = "Settings"

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                supportActionBar!!.setBackgroundDrawable(getDrawable(R.color.primaryColor))
                darkModeIcon.setTint(ContextCompat.getColor(this, R.color.white))
                feedbackIcon.setTint(ContextCompat.getColor(this, R.color.white))
                shareIcon.setTint(ContextCompat.getColor(this, R.color.white))
                rateIcon.setTint(ContextCompat.getColor(this, R.color.white))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
                darkModeIcon.setTint(ContextCompat.getColor(this, R.color.lead))
                feedbackIcon.setTint(ContextCompat.getColor(this, R.color.lead))
                shareIcon.setTint(ContextCompat.getColor(this, R.color.lead))
                rateIcon.setTint(ContextCompat.getColor(this, R.color.lead))
            }
        }

        val sharedPref1 = getSharedPreferences("darkMode", Context.MODE_PRIVATE)

        val editor1 = sharedPref1.edit()

        val isDarkMode = sharedPref1.getBoolean("isDarkMode", false)
        Constants.setDisplayMode(isDarkMode)
        binding.darkMode.isChecked = isDarkMode

        // calling the action bar
        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }



        binding.darkMode.setOnCheckedChangeListener { _, isChecked ->

            val checked = binding.darkMode.isChecked
            Constants.setDisplayMode(checked)

            editor1.apply {
                putBoolean("isDarkMode", checked)
                apply()
            }
        }
    }


    // this event will enable the back
    // function to the button on press
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }
}