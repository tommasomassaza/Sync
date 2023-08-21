package it.ter.sync.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.ChatItemBinding

class GroupDetailsAdapter (private var groupUsersList: List<String>) : RecyclerView.Adapter<GroupDetailsAdapter.ViewHolder>() {

    fun setGroupUsersList(groupUsers: List<String>) {
        groupUsersList = groupUsers
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }


    class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*
        holder.binding.apply {
            lastMessage.text = chatList[position].lastMessage
            messageTime.text = chatList[position].timeStamp
            contactName.text = chatList[position].messengerName

            if(chatList[position].image != "") {
                Glide.with(root)
                    .load(chatList[position].image)
                    .into(profileImage)
            }
        }*/
    }

    override fun getItemCount(): Int {
        return groupUsersList.size
    }
}