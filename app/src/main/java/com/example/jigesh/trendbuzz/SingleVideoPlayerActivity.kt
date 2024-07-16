package com.example.jigesh.trendbuzz

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jigesh.trendbuzz.adapter.VideoListAdapter
import com.example.jigesh.trendbuzz.databinding.ActivitySingleVideoPlayerBinding
import com.example.jigesh.trendbuzz.model.VideoModel

import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SingleVideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingleVideoPlayerBinding
    private lateinit var videoId: String
    private lateinit var adapter: VideoListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySingleVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        videoId = intent.getStringExtra("videoId")!!
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(Firebase.firestore.collection("videos")
                .whereEqualTo("videoId", videoId),
                VideoModel::class.java
            ).build()
        adapter = VideoListAdapter(options)
        binding.viewPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()  // Ensure the activity is properly closed
    }
}
