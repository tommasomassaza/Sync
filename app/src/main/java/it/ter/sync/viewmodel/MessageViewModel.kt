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
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import it.ter.sync.database.message.MessageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://sync-214bf-default-rtdb.europe-west1.firebasedatabase.app")


    var messageList: MutableLiveData<List<MessageData>> = MutableLiveData()


    fun retrieveMessages(arg: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val messagesRef = database.getReference("messages")

            // Listener per ricevere gli aggiornamenti dei messaggi
            messagesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages: MutableList<MessageData> = mutableListOf()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MessageData::class.java)
                        message?.let {
                            // Verifica se il messaggio ha senderId uguale a arg (id altro utente) o all'ID dell'utente corrente
                            // E verifica se il messaggio ha receiverId uguale a arg (id altro utente) o all'ID dell'utente corrente
                            if ((it.senderId == arg || it.senderId == user?.uid) &&
                                (it.receiverId == arg || it.receiverId == user?.uid)
                            ) {
                                messages.add(it)
                            }
                        }
                    }
                    messageList.value = messages
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }

    fun sendMessage(text: String, arg: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val messagesRef = database.getReference("messages")

            // Crea un nuovo nodo per il messaggio e aggiungi i dati
            val messageId = messagesRef.push().key
            if (messageId != null) {
                val timestamp = ServerValue.TIMESTAMP
                val message =
                    MessageData(messageId, text, timestamp.toString(), user?.uid, arg)

                // Salva il messaggio nella Firebase Realtime Database
                messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener {
                        // Messaggio inviato con successo
                        Log.i(TAG, "Messaggio inviato con successo")
                    }
                    .addOnFailureListener { error ->
                        // Gestisci l'errore di invio del messaggio
                        Log.e(TAG, "${error.message}")
                    }
            } else {
                Log.e(TAG, "Error")
            }
        }
    }
}
