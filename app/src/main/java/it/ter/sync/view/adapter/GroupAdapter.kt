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

class GroupAdapter (private var groupUsersList: List<UserData>, private var currentUser: UserData,private var messageViewModel: MessageViewModel) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {



        // Aggiungi questa variabile per memorizzare lo stato del colore
        private val selectedPositions = mutableListOf<Int>()
        private val selectedColor = 0xFF00C3FF.toInt()  // Colore selezionato #00c3ff
        private val defaultColor = 0xFFECECEC.toInt()


        fun setUsersList(groupUsers: List<UserData>) {
            groupUsersList = groupUsers
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
        val userData = groupUsersList[position]

        // Verifica se l'elemento è selezionato
        val isItemSelected = selectedPositions.contains(position)

        // Cambia il colore del layout in base allo stato dell'elemento
        val backgroundColor = if (isItemSelected) selectedColor else defaultColor
        holder.binding.root.setBackgroundColor(backgroundColor)

        holder.binding.root.setOnClickListener {
            val context = holder.binding.root.context

            val messengerId = userData.uid!!
            //Questo metodo gestisce anche la rimozione dal gruppo se l'utente era già stato selezionato
            messageViewModel.addUserToGroup(messengerId)

            Log.d("INDAGINE", "updateChat: message: $messengerId")

            if (isItemSelected) {
                selectedPositions.remove(position)
            } else {
                selectedPositions.add(position)
            }

            // Aggiorna la RecyclerView dopo il clic
            notifyDataSetChanged()
        }

        // Resto del codice per il binding dei dati nell'elemento RecyclerView
        holder.binding.apply {
            contactName.text = userData.name

            if (userData.image != "") {
                Glide.with(root)
                    .load(userData.image)
                    .into(profileImage)
            }
        }
    }





    override fun getItemCount(): Int {
        return groupUsersList.size
    }
}