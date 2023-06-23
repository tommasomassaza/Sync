package it.ter.sync.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.PostItemBinding

class PostAdapter(private val postList: List<UserData>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    class ViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var buttonChat : Button

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = postList.size

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        buttonChat = binding.chat

        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        buttonChat.setOnClickListener {
            val navController = Navigation.findNavController(it)
            val bundle = Bundle()
            bundle.putString("userId", postList[position].uid)
            bundle.putString("userName", postList[position].name)
            navController.navigate(R.id.action_homeFragment_to_messageFragment, bundle)
        }

        holder.binding.apply {
            usernamePost.text = postList[position].name
            agePost.text = postList[position].age
            locationPost.text = postList[position].location
        }
    }
}