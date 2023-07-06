package it.ter.sync.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.message.MessageData
import it.ter.sync.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application)  {

    private var TAG = this::class.simpleName

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    var chatList: MutableLiveData<List<ChatData>> = MutableLiveData()


    fun retrieveChats() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser

            val chatsRef = database.getReference("chats/${user?.uid}")

            // Listener per chat da user ai vari messenger
            chatsRef.orderByChild("timestampMillis").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chats: MutableList<ChatData> = mutableListOf()

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(ChatData::class.java)
                        chat?.let {
                            chats.add(it)
                        }
                    }

                    val sortedChats = chats.sortedByDescending { it.timestampMillis }
                    chatList.postValue(sortedChats)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }
}