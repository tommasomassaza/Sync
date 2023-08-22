package it.ter.sync.viewmodel

import android.app.Application
import android.net.Uri
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    var chatAndGroupList: MutableLiveData<List<ChatData>> = MutableLiveData()
    var groupUsersList: MutableLiveData<List<String>> = MutableLiveData()




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
                        if (chat?.group == false) {
                            chat?.let {
                                chats.add(it)
                            }
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

    fun retrieveChatsAndGroups() {
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
                    chatAndGroupList.postValue(chats.reversed())
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }

    fun filterChatsAndGroups(searchString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser

            val chatsRef = database.getReference("chats/${user?.uid}")

            // Listener per chat da user ai vari messenger
            chatsRef.orderByChild("timestampMillis").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chats: MutableList<ChatData> = mutableListOf()

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(ChatData::class.java)
                        val messengerName = chat?.messengerName ?: ""

                        if (messengerName.contains(searchString, ignoreCase = true)) {
                            chat?.let {
                                chats.add(it)
                            }
                        }
                    }
                    chatAndGroupList.postValue(chats.reversed())
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }

    fun retrieveUsersInGroup(messangerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser

            val chatsRef = database.getReference("chats/${user?.uid}/${messangerId}")

            // Listener per chat da user ai vari messenger
            chatsRef.orderByChild("timestampMillis").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existingChatData = snapshot.getValue(ChatData::class.java)
                    val usersInGroup = existingChatData?.groupMembers ?: emptyList()


                    groupUsersList.postValue(usersInGroup)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }



}