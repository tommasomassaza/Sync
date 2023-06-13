package it.ter.sync.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.viewmodel.PostViewModel

class PostAdapter(private val PostList: List<PostViewModel>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_item,parent,false)
        return PostViewHolder(itemView)
    }



    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = PostList[position]
       // holder.usernamePost.setText(currentPost.IMMAGINE COSTRUTTORE DI POSTVIWMODEL)
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val usernamePost : TextView = itemView.findViewById(R.id.username_post)
        val profileImg : ImageView = itemView.findViewById(R.id.image_post)
        val post_description : TextView = itemView.findViewById(R.id.description_post)
        val post_chat: ImageView  = itemView.findViewById(R.id.chat_post)
        val fav_chat : ImageView = itemView.findViewById(R.id.like_post)



    }
}