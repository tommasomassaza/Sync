package it.ter.sync.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.PostItemBinding

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log

class PostAdapter(private var postList: List<UserData>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    fun setPostList(users: List<UserData>) {
        postList = users
        imageBitmapList.clear() // Cancella la lista di bitmap precedente
        notifyDataSetChanged() // Aggiorna la RecyclerView

        // Converte le immagini Base64 in bitmap e aggiunge alla lista
        for (user in postList) {
            val bitmap = base64ToBitmap(user.image)

            imageBitmapList.add(bitmap)}
    }

    // Provide a reference to the views for each data item
    class ViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var buttonChat : Button

    private val imageBitmapList: MutableList<Bitmap?> = mutableListOf()

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

        // Imposta l'immagine bitmap nella vista
        val bitmap = imageBitmapList[position]

        if(bitmap != null) {
        holder.binding.imagePost.setImageBitmap(bitmap)}

    }


    fun base64ToBitmap(base64String: String): Bitmap? {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


}