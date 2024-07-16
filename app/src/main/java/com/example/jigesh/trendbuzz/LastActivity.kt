package com.example.jigesh.trendbuzz

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.jigesh.trendbuzz.databinding.ActivityLastBinding
import com.example.jigesh.trendbuzz.util.UiUtil
import com.google.firebase.auth.FirebaseAuth

class LastActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLastBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_menu_home -> {
                    UiUtil.showToast(this, "Home")
                    loadActivity(MainActivity::class.java)
                }
                R.id.bottom_menu_add_video -> {
                    UiUtil.showToast(this, "Add Video")
                    loadActivity(VideoUploadActivity::class.java)
                }
                R.id.bottom_menu_profile -> {
                    UiUtil.showToast(this, "Profile")
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid)
                    loadActivity(ProfileActivity::class.java, intent)
                }
            }
            true
        }
        // Load the default activity (home)
        if (savedInstanceState == null) {
            loadActivity(MainActivity::class.java)
        }
    }

    private fun loadActivity(activityClass: Class<*>, intent: Intent? = null) {
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
        val transaction = supportFragmentManager.beginTransaction()

        if (intent == null) {
            val fragment = ActivityFragment.newInstance(activityClass.name)
            transaction.replace(R.id.fragment_container, fragment)
        } else {
            val fragment = ActivityFragment.newInstance(activityClass.name, intent)
            transaction.replace(R.id.fragment_container, fragment)
        }

        transaction.commit()
    }

}