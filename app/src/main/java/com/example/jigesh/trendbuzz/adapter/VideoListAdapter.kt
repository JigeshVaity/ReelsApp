package com.example.jigesh.trendbuzz.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jigesh.trendbuzz.ProfileActivity
import com.example.jigesh.trendbuzz.R
import com.example.jigesh.trendbuzz.databinding.VideoItemRowBinding
import com.example.jigesh.trendbuzz.model.UserModel
import com.example.jigesh.trendbuzz.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VideoListAdapter(
    options: FirestoreRecyclerOptions<VideoModel>
) : FirestoreRecyclerAdapter<VideoModel, VideoListAdapter.VideoViewHolder>(options) {

    inner class VideoViewHolder(private val binding: VideoItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindVideo(videoModel: VideoModel) {
            // Bind user data
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get().addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = username
                        // Bind profile picture
                        Glide.with(binding.profileIcon).load(profilePic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_profile)
                            )
                            .into(binding.profileIcon)

                        binding.userDetailLayout.setOnClickListener {
                            val intent = Intent(binding.userDetailLayout.context, ProfileActivity::class.java)
                            intent.putExtra("profile_user_id", id)
                            binding.userDetailLayout.context.startActivity(intent)
                        }
                    }
                }

            binding.captionView.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE

            // Bind video
            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                }
                // Play/pause
                setOnClickListener {
                    if (isPlaying) {
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    } else {
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }

            // Set like count
            binding.likeCount.text = videoModel.likeCount.toString()

            // Update button images based on user actions
            updateButtonImages(videoModel)

            // Handle like button click
            binding.likeButton.setOnClickListener {
                handleLikeDislike(videoModel, true)
            }

            // Handle dislike button click
            binding.dislikeButton.setOnClickListener {
                handleLikeDislike(videoModel, false)
            }
        }

        private fun updateButtonImages(videoModel: VideoModel) {
            val currentUserId = Firebase.auth.currentUser?.uid ?: return
            if (videoModel.like.contains(currentUserId)) {
                binding.likeButton.setImageResource(R.drawable.likepurple)
            } else {
                binding.likeButton.setImageResource(R.drawable.like)
            }
        }

        private fun handleLikeDislike(videoModel: VideoModel, isLike: Boolean) {
            val videoRef = Firebase.firestore.collection("videos").document(videoModel.videoId)
            val currentUserId = Firebase.auth.currentUser?.uid ?: return

            Firebase.firestore.runTransaction { transaction ->
                val snapshot = transaction.get(videoRef)
                val likeCount = snapshot.getLong("likeCount") ?: 0
                val dislikeCount = snapshot.getLong("dislikeCount") ?: 0

                val likes = snapshot.get("like") as? List<String> ?: emptyList()
                val dislikes = snapshot.get("dislike") as? List<String> ?: emptyList()

                var newLikeCount = likeCount
                var newDislikeCount = dislikeCount

                if (isLike) {
                    if (likes.contains(currentUserId)) {
                        // User has already liked, so remove like
                        transaction.update(videoRef, "like", FieldValue.arrayRemove(currentUserId))
                        newLikeCount -= 1
                    } else {
                        // User has not liked, so add like
                        transaction.update(videoRef, "like", FieldValue.arrayUnion(currentUserId))
                        newLikeCount += 1
                        if (dislikes.contains(currentUserId)) {
                            // Remove dislike if user previously disliked
                            transaction.update(videoRef, "dislike", FieldValue.arrayRemove(currentUserId))
                            newDislikeCount -= 1
                        }
                    }
                } else {
                    if (dislikes.contains(currentUserId)) {
                        // User has already disliked, so remove dislike
                        transaction.update(videoRef, "dislike", FieldValue.arrayRemove(currentUserId))
                        newDislikeCount -= 1
                    } else {
                        // User has not disliked, so add dislike
                        transaction.update(videoRef, "dislike", FieldValue.arrayUnion(currentUserId))
                        newDislikeCount += 1
                        if (likes.contains(currentUserId)) {
                            // Remove like if user previously liked
                            transaction.update(videoRef, "like", FieldValue.arrayRemove(currentUserId))
                            newLikeCount -= 1
                        }
                    }
                }

                // Ensure counts don't go below zero
                transaction.update(videoRef, "likeCount", newLikeCount.coerceAtLeast(0))
                transaction.update(videoRef, "dislikeCount", newDislikeCount.coerceAtLeast(0))
            }.addOnSuccessListener {
                Log.d("VideoListAdapter", "Transaction success: $currentUserId ${if (isLike) "liked" else "disliked"} video ${videoModel.videoId}")
                // Update button images after successful transaction
                updateButtonImages(videoModel)
            }.addOnFailureListener { e ->
                Log.e("VideoListAdapter", "Transaction failure", e)
            }

            // Update UI immediately
            if (isLike) {
                if (videoModel.like.contains(currentUserId)) {
                    videoModel.likeCount -= 1
                    videoModel.like = videoModel.like - currentUserId
                } else {
                    videoModel.likeCount += 1
                    videoModel.like = videoModel.like + currentUserId
                    if (videoModel.dislike.contains(currentUserId)) {
                        videoModel.dislikeCount -= 1
                        videoModel.dislike = videoModel.dislike - currentUserId
                    }
                }
            } else {
                if (videoModel.dislike.contains(currentUserId)) {
                    videoModel.dislikeCount -= 1
                    videoModel.dislike = videoModel.dislike - currentUserId
                } else {
                    videoModel.dislikeCount += 1
                    videoModel.dislike = videoModel.dislike + currentUserId
                    if (videoModel.like.contains(currentUserId)) {
                        videoModel.likeCount -= 1
                        videoModel.like = videoModel.like - currentUserId
                    }
                }
            }
            binding.likeCount.text = videoModel.likeCount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bindVideo(model)
    }
}
