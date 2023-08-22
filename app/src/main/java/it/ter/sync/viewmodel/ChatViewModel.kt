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
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.user.UserData
import it.ter.sync.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application)  {

    private var TAG = this::class.simpleName

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()


    var chatList: MutableLiveData<List<ChatData>> = MutableLiveData()
    var chatAndGroupList: MutableLiveData<List<ChatData>> = MutableLiveData()
    var groupUsersList: MutableLiveData<List<UserData>> = MutableLiveData()

    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()




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

    fun retrieveUsersWhoChattedWithMe() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser

            val currentList: MutableList<UserData> = mutableListOf()
            val chatsRef = database.getReference("chats/${user?.uid}")

            Log.i("USER", "${user?.uid}")
            // Listener per chat da user ai vari messenger
            chatsRef.orderByChild("timestampMillis").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(ChatData::class.java)
                        if (chat?.group == false) {
                            val secondElementId = chatSnapshot.key
                            Log.i("SECONDELEMENT", "$secondElementId")
                            // Ora hai il secondo elemento (l'id del messanger) con cui l'utente ha chattato
                            // Puoi fare ciò che desideri con questo ID.
                            if (secondElementId != null) {
                                fireStore.collection("users")
                                    .document(secondElementId)
                                    .get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        if (documentSnapshot.exists()) {
                                            val name = documentSnapshot.getString("name") ?: ""
                                            val age = documentSnapshot.getString("age") ?: ""
                                            val location =
                                                documentSnapshot.getString("location") ?: ""
                                            val image = documentSnapshot.getString("image") ?: ""
                                            val tag = documentSnapshot.getString("tag") ?: ""
                                            val tag2 = documentSnapshot.getString("tag2") ?: ""
                                            val tag3 = documentSnapshot.getString("tag3") ?: ""
                                            val stato = documentSnapshot.getString("stato") ?: ""
                                            val privatetag1 =
                                                documentSnapshot.getString("privatetag1") ?: ""
                                            val privatetag2 =
                                                documentSnapshot.getString("privatetag2") ?: ""
                                            val privatetag3 =
                                                documentSnapshot.getString("privatetag3") ?: ""
                                            val userData = UserData(
                                                uid = secondElementId,
                                                name = name,
                                                location = location,
                                                age = age,
                                                image = image,
                                                tag = tag,
                                                tag2 = tag2,
                                                tag3 = tag3,
                                                stato = stato,
                                                privatetag1 = privatetag1,
                                                privatetag2 = privatetag2,
                                                privatetag3 = privatetag3
                                            )

                                            Log.i("USERDATA", "${userData.name}")


                                            if (currentList.none { it.uid == userData.uid }) {
                                                currentList.add(userData)
                                            }

                                                Log.i("UPDATED LIST", "${currentList}")
                                                // Ora updatedList contiene userData solo se non era già presente

                                            // Aggiorna groupUsersList con la nuova lista
                                            groupUsersList.postValue(currentList)
                                            Log.i("LA CURRENT", "${currentList}")
                                            Log.i("GROUP LIST", "${groupUsersList.value}")
                                            }

                                    }
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })

        }

    }





}