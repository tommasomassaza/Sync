package it.ter.sync.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.message.MessageData
import it.ter.sync.databinding.ChatItemBinding
import it.ter.sync.databinding.MessageItemBinding

class ChatAdapter (private var chatList: List<ChatData>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    fun setChatList(chats: List<ChatData>) {
        chatList = chats
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.setOnClickListener {
            val navController = Navigation.findNavController(it)
            val bundle = Bundle()
            bundle.putString("userId", chatList[position].uid)
            bundle.putString("userName", chatList[position].messengerName)
            navController.navigate(R.id.action_chatFragment_to_messageFragment, bundle)
        }

        holder.binding.apply {
            lastMessage.text = chatList[position].lastMessage
            messageTime.text = chatList[position].timeStamp
            contactName.text = chatList[position].messengerName
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}