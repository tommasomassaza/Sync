package it.ter.sync.viewmodel

import android.app.Application
import android.util.Log
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
import it.ter.sync.database.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val messageRepository: MessageRepository = MessageRepository(application)

    private var user: FirebaseUser? = null
    private var messengerId: String? = ""
    private var messengerName: String? = ""
    private var lastTimestamp: Long = 0

    var messageList: MutableLiveData<List<MessageData>> = MutableLiveData<List<MessageData>>().apply {
        value = emptyList()
    }

    fun retrieveMessages(messengerId: String?, messengerName: String){
        viewModelScope.launch(Dispatchers.IO) {
            user = firebaseAuth.currentUser
            this@MessageViewModel.messengerId = messengerId
            this@MessageViewModel.messengerName = messengerName

            // Prima prendo i messaggi in locale
            messageList.postValue(
                messageRepository.getMessagesBySenderAndReceiver(user?.uid ?: "", messengerId ?: ""))

            // last message in local
            val lastMessageInLocal = messageRepository.getLastMessage(user?.uid ?: "",messengerId ?: "")
            lastTimestamp = lastMessageInLocal?.timestampMillis ?: 0

            // last message in Firebase
            val chatId = generateChatId(user?.uid ?: "",messengerId ?: "")
            val messagesRef = database.getReference("messages/${chatId}")
            messagesRef.orderByChild("timestampMillis")
                .limitToFirst(1)
                .get()
                .addOnSuccessListener {
                    val lastTimestampFirebase = it.getValue(MessageData::class.java)?.timestampMillis ?: 0
                    if(lastTimestampFirebase > lastTimestamp){
                        retrieveMessagesInFirebase(chatId)
                    }
                }
                .addOnFailureListener {
                    retrieveMessagesInFirebase(chatId)
                }
        }
    }

    private fun retrieveMessagesInFirebase(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val messagesRef = database.getReference("messages/${chatId}")

            // Listener per messaggi da user a messenger
            messagesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.postValue(messageList.value?.plus(updateMessage(snapshot)))
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }

    private fun updateMessage(snapshot: DataSnapshot): MutableList<MessageData> {
        val messages: MutableList<MessageData> = mutableListOf()
        var lastMessage : MessageData? = null
        for (messageSnapshot in snapshot.children) {
            val message = messageSnapshot.getValue(MessageData::class.java)
            message?.let {
                if(it.timestampMillis > lastTimestamp){
                    messages.add(it)
                    lastMessage = it
                    viewModelScope.launch(Dispatchers.IO) {
                        // Update in local
                        messageRepository.insertMessage(it)
                    }
                }
            }
        }
        lastTimestamp = lastMessage?.timestampMillis ?: 0

        lastMessage?.let { updateChat(it) }

        return messages
    }

    private fun updateChat(lastMessage: MessageData) {
        viewModelScope.launch(Dispatchers.IO) {
            val chatRef = database.getReference("chats/${user?.uid}/${messengerId}")

            val chat = ChatData(messengerId!!, lastMessage.text, lastMessage.timeStamp, lastMessage.timestampMillis, messengerName)

            chatRef.setValue(chat)
                .addOnSuccessListener {
                    Log.i(TAG, "Chat update")
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "${error.message}")
                }
        }
    }

    fun sendMessage(text: String, messengerId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val chatId = generateChatId(user?.uid ?: "",messengerId ?: "")
            val messagesRef = database.getReference("messages/${chatId}")

            // Crea un nuovo nodo per il messaggio e aggiungi i dati
            val messageId = messagesRef.push().key
            if (messageId != null) {
                val timestampMillis = System.currentTimeMillis()

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Rome") // Imposta il fuso orario su Italia
                val dateString = dateFormat.format(Date(timestampMillis))

                val message = MessageData(messageId, text, timestampMillis, dateString, user?.uid, messengerId)

                // Salva il messaggio nella Firebase Realtime Database
                messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener {
                        Log.i(TAG, "Messaggio inviato con successo")
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "${error.message}")
                    }
            } else {
                Log.e(TAG, "Error")
            }
        }
    }

    private fun generateChatId(string1: String, string2: String): String {
        val minString = if (string1 < string2) string1 else string2
        val maxString = if (minString == string1) string2 else string1

        return minString + maxString
    }
}