package com.dishant26201.wordquiz

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dishant26201.quizapp.Constants
import com.dishant26201.wordquiz.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {


    private val EMAIL : String = "dishant26201@gmail.com"
    private val EMAIL_SUBJECT : String = "Vocab Buddy Feedback"
    private val EMAIL_CHOOSER : String = "SEND EMAIL"
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = "Settings"

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                supportActionBar!!.setBackgroundDrawable(getDrawable(R.color.primaryColor))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
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


        binding.rl2.setOnClickListener {
            val uriText = "mailto:" + EMAIL + "?subject=" + EMAIL_SUBJECT
            val mailUri: Uri = Uri.parse(uriText)
            val emailIntent = Intent(Intent.ACTION_SENDTO, mailUri)
            emailIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(Intent.createChooser(emailIntent, EMAIL_CHOOSER))
        }

        binding.rl3.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
                var shareMessage = "\nCheck out this App!\n"
                shareMessage =
                    """
                    ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                    
                    
                    """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                e.toString();
            }
        }

        binding.rl4.setOnClickListener {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}") // missing 'http://' will cause crashed
            val playStoreIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(playStoreIntent)
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
}