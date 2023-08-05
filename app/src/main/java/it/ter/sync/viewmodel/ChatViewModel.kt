package it.ter.sync.viewmodel

import android.app.Application
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    private var user: FirebaseUser? = null
    private var messengerId: String? = "group"


    private var groupIDs: ArrayList<String> = ArrayList()


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
                    chatList.postValue(chats.reversed())
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }

    fun addUserToGroup(messangerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            groupIDs.add(messangerId)
        }
    }


    fun createGroupWithUsers(messengerImageUrl: String) {

        //WORK IN PROGRESS
        viewModelScope.launch(Dispatchers.IO) {
            val chatUserRef = database.getReference("chats/${user?.uid}/${messengerId}")
            val chatUser = ChatData(messengerId!!, messengerImageUrl, "", "", "", "","group")
            chatUserRef.setValue(chatUser)

            val chatMessengerRef = database.getReference("chats/${messengerId}/${user?.uid}")
            val chatMessenger = ChatData(user?.uid!!, messengerImageUrl, "", "", "", "currentUserName","group")
            chatMessengerRef.setValue(chatMessenger)
        }
    }

}