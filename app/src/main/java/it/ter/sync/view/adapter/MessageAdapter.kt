package it.ter.sync.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import it.ter.sync.database.message.MessageData
import it.ter.sync.databinding.MessageSentItemBinding
import it.ter.sync.databinding.MessageReceivedItemBinding


class MessageAdapter(
    private var messageList: List<MessageData>,
    private val messengerId: String,
    private val messengerName: String) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {


    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }


    override fun getItemViewType(position: Int): Int {
        val messageData = messageList[position]
        return if (messengerId == messageData.senderId) {
            TYPE_SENT // Messaggio inviato dall'utente loggato
        } else {
            TYPE_RECEIVED // Messaggio inviato da altri utenti
        }
    }



    fun setMessageList(messages: List<MessageData>) {
        messageList = messages
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewBinding // Utilizziamo ViewBinding come tipo generico

        if (viewType == TYPE_SENT) {
            // Messaggio inviato dall'utente loggato
            binding = MessageSentItemBinding.inflate(inflater, parent, false)
        } else {
            // Messaggio inviato da altri utenti
            binding = MessageReceivedItemBinding.inflate(inflater, parent, false)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageData = messageList[position]
        if (holder.binding is MessageSentItemBinding) {
            // Messaggio inviato dall'utente loggato
            holder.binding.textViewSender.text = messengerName
            holder.binding.textViewMessage.text = messageData.text
            holder.binding.textViewTime.text = messageData.timeStamp
        } else if (holder.binding is MessageReceivedItemBinding) {
            // Messaggio inviato da altri utenti
            holder.binding.textViewSender.text = "Tu"
            holder.binding.textViewMessage.text = messageData.text
            holder.binding.textViewTime.text = messageData.timeStamp
        }
    }


    override fun getItemCount(): Int {
            return messageList.size
        }
}