package it.ter.sync.view.adapter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.ChatItemBinding
import it.ter.sync.databinding.GroupItemBinding
import it.ter.sync.view.MainActivity
import it.ter.sync.viewmodel.ChatViewModel
import it.ter.sync.viewmodel.MessageViewModel
import it.ter.sync.viewmodel.NotificationViewModel

class GroupAdapter (private var chatList: List<ChatData>, private var currentUser: UserData,private var messageViewModel: MessageViewModel) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {



        // Aggiungi questa variabile per memorizzare lo stato del colore
        private var selectedPosition = -1
        private val selectedColor = 0xFF00C3FF.toInt()  // Colore selezionato #00c3ff
        private val defaultColor = 0xFFECECEC.toInt()

    // Lista di stringhe
    val groupIDs: ArrayList<String> = ArrayList()


        fun setChatList(chats: List<ChatData>) {
        chatList = chats
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    fun setCurrentUser(currentUser: UserData?) {
        if (currentUser != null) {
            this@GroupAdapter.currentUser = currentUser
        }
    }

    class ViewHolder(val binding: GroupItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.root.setOnClickListener {
            val context = holder.binding.root.context


            val messengerId = chatList[position].uid!!
            messageViewModel.addUserToGroup(messengerId)
            Log.d("INDAGINE", "updateChat: message: $messengerId")


            // Verifica se l'elemento è già stato selezionato
            val isItemSelected = selectedPosition == position

            // Cambia il colore del layout quando viene fatto clic sopra l'elemento
            val newColor = if (isItemSelected) selectedColor
            else defaultColor

            holder.binding.root.setBackgroundColor(newColor)

            // Memorizza lo stato del colore selezionato
            if (isItemSelected) {
                selectedPosition =
                    -1 // Torna allo stato predefinito se l'elemento è già stato selezionato
            } else {
                selectedPosition = position // Memorizza la posizione dell'elemento selezionato
            }

            // Aggiorna la RecyclerView dopo il clic
            notifyDataSetChanged()

        }

        // Resto del codice per il binding dei dati nell'elemento RecyclerView
        holder.binding.apply {
            //lastMessage.text = currentUser.stato
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