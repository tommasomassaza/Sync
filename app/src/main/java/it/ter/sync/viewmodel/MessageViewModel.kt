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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.notify.NotificationType
import it.ter.sync.database.repository.MessageRepository
import it.ter.sync.utils.Utils
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
    private var currentUserName: String? = ""
    private var lastTimestamp: Long = 0

    var messageList: MutableLiveData<List<MessageData>> = MutableLiveData()

    private var messageRef: DatabaseReference? = null

    fun retrieveMessages(messengerId: String?, messengerName: String, currentUserName: String){
        viewModelScope.launch(Dispatchers.IO) {
            user = firebaseAuth.currentUser
            this@MessageViewModel.messengerId = messengerId
            this@MessageViewModel.messengerName = messengerName
            this@MessageViewModel.currentUserName = currentUserName

            // Prima prendo i messaggi in locale
            messageList.postValue(
                messageRepository.getMessagesBySenderAndReceiver(user?.uid ?: "", messengerId ?: ""))

            // last message in local
            val lastMessageInLocal =
                messageRepository.getLastMessage(user?.uid ?: "", messengerId ?: "")
            lastTimestamp = lastMessageInLocal?.timestampMillis?.toLong() ?: 0


            // Rimuovi il listener precedente se presente
            messageRef?.removeEventListener(valueEventListener)

            val chatId = Utils.generateChatId(user?.uid ?: "",messengerId ?: "")
            messageRef = database.getReference("messages/${chatId}")

            messageRef!!.orderByChild("timestampMillis").startAfter(lastTimestamp.toString()).addValueEventListener(valueEventListener)
        }
    }

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            messageList.postValue(messageList.value?.plus(updateMessage(snapshot)))
        }

        override fun onCancelled(error: DatabaseError) {
            // Gestisci l'errore di recupero dei messaggi
            Log.i(TAG, error.message)
        }
    }

    private fun updateMessage(snapshot: DataSnapshot): MutableList<MessageData> {
        val messages: MutableList<MessageData> = mutableListOf()
        var lastMessage = MessageData()
        for (messageSnapshot in snapshot.children) {
            val message = messageSnapshot.getValue(MessageData::class.java)
            message?.let {
                if(it.timestampMillis.toLong() > lastTimestamp){
                    messages.add(it)
                    lastMessage = it
                    viewModelScope.launch(Dispatchers.IO) {
                        // Update in local
                        messageRepository.insertMessage(it)
                    }

                }
            }
        }
        // Se sono entrato nel'if del for
        if(lastTimestamp < lastMessage.timestampMillis.toLong()) {
            lastTimestamp = lastMessage.timestampMillis.toLong()
        }

        return messages
    }

    fun sendMessage(text: String, messengerId: String?, userImageUrl: String, messengerImageUrl: String, groupIDs: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val chatId = Utils.generateChatId(user?.uid ?: "",messengerId ?: "")
            val messagesRef = database.getReference("messages/${chatId}")

            val messageId = messagesRef.push().key
            if (messageId != null) {
                val timestampMillis = System.currentTimeMillis()

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Rome") // Imposta il fuso orario su Italia
                val dateString = dateFormat.format(Date(timestampMillis))

                val message = MessageData(messageId, userImageUrl, text, timestampMillis.toString(), dateString, user?.uid, messengerId, groupIDs)

                // Salva il messaggio nella Firebase Realtime Database
                messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener {
                        Log.i(TAG, "Messaggio inviato con successo")

                        updateChat(message, messengerImageUrl)
                        addMessageNotification(message)
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "${error.message}")
                    }
            } else {
                Log.e(TAG, "Error")
            }
        }
    }

    private fun updateChat(message: MessageData, messengerImageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val chatUserRef = database.getReference("chats/${user?.uid}/${messengerId}")
            val chatUser = ChatData(messengerId!!, messengerImageUrl, message.text, message.timeStamp, message.timestampMillis, messengerName,messengerId)
            chatUserRef.setValue(chatUser)

            val chatMessengerRef = database.getReference("chats/${messengerId}/${user?.uid}")
            val chatMessenger = ChatData(user?.uid!!, message.image, message.text, message.timeStamp, message.timestampMillis, currentUserName,messengerId)
            chatMessengerRef.setValue(chatMessenger)
        }
    }

    private fun addMessageNotification(lastMessage: MessageData) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${messengerId}/${user?.uid}/message")

            val timestampMillis = System.currentTimeMillis()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateFormat.timeZone =
                TimeZone.getTimeZone("Europe/Rome") // Imposta il fuso orario su Italia
            val dateString = dateFormat.format(Date(timestampMillis))

            val notification = NotificationData(
                NotificationType.MESSAGE,
                lastMessage.image,
                lastMessage.text,
                dateString,
                timestampMillis.toString(),
                false,
                user?.uid,
                currentUserName
            )

            // Salva il messaggio nella Firebase Realtime Database
            ref.setValue(notification)
                .addOnSuccessListener {
                    Log.i(TAG, "Notifica inviata con successo")
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "${error.message}")
                }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val chatId = Utils.generateChatId(user?.uid ?: "", messengerId ?: "")
            val messagesRef = database.getReference("messages/${chatId}/$messageId")

            // Rimuovi il messaggio dalla Firebase Realtime Database
            messagesRef.removeValue()
                .addOnSuccessListener {
                    Log.i(TAG, "Messaggio eliminato con successo da firestore")
                    viewModelScope.launch(Dispatchers.IO) {
                        messageRepository.deleteMessageById(messageId)
                    }
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "${error.message}")
                }
        }
    }
}
