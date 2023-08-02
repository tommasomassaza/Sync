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

class GroupAdapter (private var chatList: List<ChatData>, private var currentUser: UserData) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {



        // Aggiungi questa variabile per memorizzare lo stato del colore
        private var selectedPosition = -1
        private val defaultColor = R.color.black // Imposta il colore predefinito qui
        private val selectedColor = R.color.purple_200


        fun setChatList(chats: List<ChatData>) {
        chatList = chats
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    fun setCurrentUser(currentUser: UserData?) {
        if (currentUser != null) {
            this@GroupAdapter.currentUser = currentUser
        }
    }

    class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.setOnClickListener {
            val context = holder.binding.root.context

            // Verifica se l'elemento è già stato selezionato
            val isItemSelected = selectedPosition == position

            // Cambia il colore del layout quando viene fatto clic sopra l'elemento
            val newColor = if (isItemSelected) context.resources.getColor(defaultColor)
            else context.resources.getColor(selectedColor)

            holder.binding.root.setBackgroundColor(newColor)

            // Memorizza lo stato del colore selezionato
            if (isItemSelected) {
                selectedPosition =
                    -1 // Torna allo stato predefinito se l'elemento è già stato selezionato
            } else {
                selectedPosition = position // Memorizza la posizione dell'elemento selezionato
            }

            // Imposta il colore del layout in base allo stato dell'elemento selezionato
            holder.binding.root.setBackgroundColor(
                if (selectedPosition == position) context.resources.getColor(selectedColor)
                else context.resources.getColor(defaultColor)
            )
        }

        // Resto del codice per il binding dei dati nell'elemento RecyclerView
        holder.binding.apply {
            lastMessage.text = currentUser.stato
            //messageTime.text = chatList[position].timeStamp
            contactName.text = chatList[position].messengerName

            if (chatList[position].image != "") {
                Glide.with(root)
                    .load(chatList[position].image)
                    .into(profileImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}