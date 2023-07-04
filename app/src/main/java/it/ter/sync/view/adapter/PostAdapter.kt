package it.ter.sync.view.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.PostItemBinding

import com.bumptech.glide.Glide
import it.ter.sync.viewmodel.NotificationViewModel

class PostAdapter(private var postList: List<UserData>, private var currentUser: UserData, private var notificationViewModel: NotificationViewModel) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    fun setPostList(users: List<UserData>) {
        postList = users

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
                bundle.putString("imageUrl", currentUser.image)
                navController.navigate(R.id.action_homeFragment_to_messageFragment, bundle)
            }

            holder.binding.like.setOnClickListener {
                notificationViewModel.addLikeNotification(postList[position].uid,currentUser.name,currentUser.image)
            }


            holder.binding.apply {
                usernamePost.text = postList[position].name
                agePost.text = postList[position].age
                locationPost.text = postList[position].location
                tagsView.setText("#"+postList[position].tag + " #" + postList[position].tag2 +" #"+postList[position].tag3)





                if(postList[position].image != "") {
                    Glide.with(root)
                        .load(postList[position].image)
                        .into(imagePost)
                }
            }
        }
}