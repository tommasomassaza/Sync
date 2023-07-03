package it.ter.sync.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.database.message.MessageData
import it.ter.sync.databinding.MessageItemBinding

class MessageAdapter(
    private var messageList: List<MessageData>,
    private val messengerId: String,
    private val messengerName: String) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    fun setMessageList(messages: List<MessageData>) {
        messageList = messages
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    class ViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.apply {
                if(messengerId == messageList[position].senderId)
                    textViewSender.text = messengerName
                else
                    textViewSender.text = "Tu"

                textViewMessage.text = messageList[position].text
                textViewTime.text = messageList[position].timeStamp
            }
        }

        override fun getItemCount(): Int {
            return messageList.size
        }
}