package it.ter.sync.view.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.PostItemBinding

import com.bumptech.glide.Glide
import it.ter.sync.viewmodel.NotificationViewModel

class PostAdapter(private var postList: List<UserData>, private var likeList: List<String>, private var currentUser: UserData, private var notificationViewModel: NotificationViewModel) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    fun setPostList(users: List<UserData>) {
        postList = users

        notifyDataSetChanged() // Aggiorna la RecyclerView
    }
    fun setLikeList(likes: List<String>) {
        likeList = likes

        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    fun setCurrentUser(currentUser: UserData?) {
        if (currentUser != null) {
            this@PostAdapter.currentUser = currentUser
        }
    }

    // Provide a reference to the views for each data item
    class ViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root)

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = postList.size

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }


    // Replace the contents of a view (invoked by the layout manager)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.chat.setOnClickListener {
                val navController = Navigation.findNavController(it)
                val bundle = Bundle()
                bundle.putString("messengerId", postList[position].uid)
                bundle.putString("messengerName", postList[position].name)
                bundle.putString("currentUserName", currentUser.name)
                bundle.putString("userImageUrl", currentUser.image)
                bundle.putString("messengerImageUrl", postList[position].image)
                navController.navigate(R.id.action_homeFragment_to_messageFragment, bundle)
            }

            holder.binding.like.setOnClickListener {
                if (likeList.contains(postList[position].uid)) {
                    notificationViewModel.deleteLikeNotification(postList[position].uid)
                    it.setBackgroundResource(R.drawable.round_button)
                } else {
                    notificationViewModel.addLikeNotification(postList[position].uid, currentUser.name, currentUser.image)
                    it.setBackgroundResource(R.drawable.round_button_selected)
                }
            }


            holder.binding.apply {
                usernamePost.text = postList[position].name
                agePost.text = postList[position].age
                locationPost.text = postList[position].location

                if(postList[position].tag.isEmpty() || postList[position].tag2.isEmpty() || postList[position].tag3.isEmpty()) {
                    sad.visibility = View.VISIBLE
                    tagsView.setText("\n"+ postList[position].name+" non ha inserito interessi")
                }else{
                    sad.visibility = View.INVISIBLE
                    tagsView.setText("#"+postList[position].tag + " #" + postList[position].tag2 +" #"+postList[position].tag3)}





                Glide.with(root)
                    .load(postList[position].image)
                    .error(R.mipmap.ic_launcher)
                    .into(imagePost)
            }
        }
}