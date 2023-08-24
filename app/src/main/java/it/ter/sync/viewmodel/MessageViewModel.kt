package it.ter.sync.viewmodel

import android.app.Application
import android.net.Uri
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.notify.NotificationType
import it.ter.sync.database.repository.MessageRepository
import it.ter.sync.database.user.UserData
import it.ter.sync.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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


    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var groupUsersList: MutableLiveData<List<UserData>> = MutableLiveData()


    var groupCreated: MutableLiveData<Boolean> = MutableLiveData()
    var imageString: MutableLiveData<String> = MutableLiveData()
    var imageStringDetails: MutableLiveData<String> = MutableLiveData()

    // Dichiarazione del LiveData booleano
    var myBooleanLiveData = MutableLiveData<Boolean>()

    private var messageRef: DatabaseReference? = null
    private var chatGroupRef: DatabaseReference? = null

    private var groupMembersIds: ArrayList<String> = ArrayList()

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


            isChatGroup(user?.uid, messengerId) { isGroup ->
                // Adesso hai il valore "isGroup" che indica se la chat è un gruppo o meno
                // Puoi usarlo per prendere decisioni basate sul tipo di chat
                // Esegui qui il codice che dipende dal risultato
                myBooleanLiveData.value  = isGroup

                Log.i("IS_GROUP", "${ myBooleanLiveData.value} ")
                Log.i("IS_GROUP2", "${ isGroup} ")
            }

        }
    }


    fun isChatGroup(UserUid: String?,messengerId: String?,onResult: (Boolean) -> Unit) {

        chatGroupRef = database.getReference("chats/${user?.uid}/${messengerId}")

        chatGroupRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatData = snapshot.getValue(ChatData::class.java)
                Log.i("BIS CHATDATA ", "${chatData?.group} ")
                val isGroup = chatData?.group ?: false
                onResult(isGroup)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                onResult(false)
            }
        })
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

    fun sendMessage(text: String, messengerId: String?, userImageUrl: String, messengerImageUrl: String,senderName: String) {
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

                val message = MessageData(messageId, userImageUrl, text, timestampMillis.toString(), dateString, user?.uid,messageId,senderName)

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
            val chatUser = ChatData(messengerId!!, messengerImageUrl, message.text, message.timeStamp, message.timestampMillis, messengerName)
            chatUserRef.setValue(chatUser)

            val chatMessengerRef = database.getReference("chats/${messengerId}/${user?.uid}")
            val chatMessenger = ChatData(user?.uid!!, message.image, message.text, message.timeStamp, message.timestampMillis, currentUserName)
            chatMessengerRef.setValue(chatMessenger)

        }
    }


    fun addUserToGroup(messangerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            //se l'utente era già presente nella lista lo rimuovo, perché significa che è stato deselezionato
            if (messangerId in groupMembersIds) {
                groupMembersIds.remove(messangerId)
            } else {
                //altrimenti lo aggiungo perché significa che è stato selezionato
                groupMembersIds.add(messangerId)
            }
        }
    }

    fun createGroupWithUsers(groupImageUrl: Uri?, groupName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Ottieni il riferimento al nodo "groups" nel database
            val groupsRef = database.getReference("chats")

            // Genera un nuovo ID univoco per il gruppo
            val groupId = groupsRef.push().key ?: ""

            // Crea l'oggetto GroupData con le informazioni del gruppo
            val groupData =
                ChatData(groupId, groupImageUrl.toString(), "", "", "", groupName, true, groupMembersIds)

            // Salva il gruppo nel database utilizzando l'ID appena generato
            groupsRef.child(groupId).setValue(groupData)
                .addOnSuccessListener {
                    Log.i(TAG, "Gruppo creato con successo")
                    groupCreated.postValue(true)
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Errore durante la creazione del gruppo: ${error.message}")
                    groupCreated.postValue(false)
                }

            sendMessageGroupCreate("Benvenuto!", groupId, groupImageUrl, groupName)
            //metto groupIDs nell'entità chat

            //metto me stesso in groupMembers
            val user = firebaseAuth.currentUser
            groupMembersIds.add(user!!.uid)
        }

    }

     fun sendMessageGroup(text: String, groupId: String?, groupImageUrl: String, groupName: String,senderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (groupId != null) {
                val user = firebaseAuth.currentUser
                // Fai una copia della lista groupMembers per evitare ConcurrentModificationException
                val membersCopy = ArrayList(groupMembersIds)
                val chatId = Utils.generateChatId(user?.uid ?: "", groupId)
                val messagesRef = database.getReference("messages/${chatId}")

                val messageId = messagesRef.push().key
                if (messageId != null) {
                    val timestampMillis = System.currentTimeMillis()
                    val dateString = getDateString(timestampMillis)

                    val message = MessageData(
                        messageId,
                        groupImageUrl,
                        text,
                        timestampMillis.toString(),
                        dateString,
                        user?.uid,
                        groupId,
                        senderName
                    )

                    // Salva il messaggio nella Firebase Realtime Database
                    messagesRef.child(messageId).setValue(message)
                        .addOnSuccessListener {
                            Log.i("GROOPON", "Messaggio inviato con successo al gruppo")


                            updateChatGroup(user!!.uid, groupId, message, groupName)

                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "${error.message}")
                        }
                }

                Log.i("SONO QUI SEND", "$groupMembersIds")

                //Invia il messaggio dal gruppo a tutti
                val chatUserRef = database.getReference("chats/${user!!.uid}/$groupId")

                chatUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingChatData = snapshot.getValue(ChatData::class.java)
                        val existingGroupMembers = existingChatData?.groupMembers ?: emptyList()

                        Log.i("SONO QUI SEND", "$existingGroupMembers")

                        for (memberId in existingGroupMembers) {
                            if (memberId != user!!.uid) {
                                Log.i("SONO QUI SEND", "Messaggio inviato con successo dal gruppo")

                                val chatId = Utils.generateChatId(groupId, memberId)
                                val messagesRef = database.getReference("messages/${chatId}")

                                val messageId = messagesRef.push().key
                                if (messageId != null) {
                                    val timestampMillis = System.currentTimeMillis()
                                    val dateString = getDateString(timestampMillis)

                                    val message = MessageData(
                                        messageId,
                                        groupImageUrl,
                                        text,
                                        timestampMillis.toString(),
                                        dateString,
                                        groupId,
                                        memberId,
                                        senderName
                                    )

                                    // Salva il messaggio nella Firebase Realtime Database
                                    messagesRef.child(messageId).setValue(message)
                                        .addOnSuccessListener {
                                            Log.i("DAL GRUPPO", "Messaggio inviato con successo dal gruppo")


                                            // Per ogni membro del gruppo, aggiorna la chat con il nuovo messaggio (anche per se stesso)

                                            updateChatGroup(
                                                memberId,
                                                groupId,
                                                message,
                                                groupName,
                                            )

                                        }
                                        .addOnFailureListener { error ->
                                            Log.e("NON SONO RIUSCITO", "${error.message}")
                                        }
                                }
                            }
                        }

                        Log.e("VALORE VECCHIO", "${existingGroupMembers}")
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }})



            } else {
                Log.e(TAG, "L'ID del gruppo è nullo")
            }
        }
    }


    fun sendMessageGroupCreate(text: String, groupId: String?, groupImageUrl: Uri?, groupName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (groupId != null) {
                val user = firebaseAuth.currentUser
                // Fai una copia della lista groupMembers per evitare ConcurrentModificationException
                val membersCopy = ArrayList(groupMembersIds)
                val chatId = Utils.generateChatId(user?.uid ?: "", groupId)
                val messagesRef = database.getReference("messages/${chatId}")

                val messageId = messagesRef.push().key
                if (messageId != null) {
                    val timestampMillis = System.currentTimeMillis()
                    val dateString = getDateString(timestampMillis)

                    val message = MessageData(
                        messageId,
                        groupImageUrl.toString(),
                        text,
                        timestampMillis.toString(),
                        dateString,
                        user?.uid,
                        groupId,
                        "Benvenuto"
                    )

                    // Salva il messaggio nella Firebase Realtime Database
                    messagesRef.child(messageId).setValue(message)
                        .addOnSuccessListener {
                            Log.i("INVIO_GRUPPO_CREATE", "Messaggio inviato con successo al gruppo")


                            updateChatGroupCreate(user!!.uid, groupId, message, "", groupName, membersCopy)
                            updateGroupImage(user!!.uid, groupId, groupImageUrl)

                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "${error.message}")
                        }
                }

                Log.i("SONO QUI CREATE", "$groupMembersIds")

                //Invia il messaggio dal gruppo a tutti
                for (memberId in membersCopy) {
                    if (memberId != user!!.uid) {
                        Log.i("SONO QUI CREATE", "Messaggio inviato con successo dal gruppo")

                        val chatId = Utils.generateChatId(groupId, memberId)
                        val messagesRef = database.getReference("messages/${chatId}")

                        val messageId = messagesRef.push().key
                        if (messageId != null) {
                            val timestampMillis = System.currentTimeMillis()
                            val dateString = getDateString(timestampMillis)

                            val message = MessageData(
                                messageId,
                                groupImageUrl.toString(),
                                text,
                                timestampMillis.toString(),
                                dateString,
                                groupId,
                                memberId,
                                "Benvenuto"
                            )

                            // Salva il messaggio nella Firebase Realtime Database
                            messagesRef.child(messageId).setValue(message)
                                .addOnSuccessListener {
                                    Log.i("DAL GRUPPO", "Messaggio inviato con successo dal gruppo")


                                    // Per ogni membro del gruppo, aggiorna la chat con il nuovo messaggio (anche per se stesso)
                                    //updateChatGroup(user!!.uid, groupId, message, groupImageUrl, groupName)

                                    updateChatGroupCreate(
                                        memberId,
                                        groupId,
                                        message,
                                        "",
                                        groupName,
                                        membersCopy
                                    )

                                    updateGroupImage(memberId, groupId, groupImageUrl)

                                }
                                .addOnFailureListener { error ->
                                    Log.e("NON SONO RIUSCITO", "${error.message}")
                                }
                        }
                    }
                }
                //pulisco la lista fissa che conteneva gli IDs una volta creato il gruppo
                groupMembersIds.clear()
            } else {
                Log.e(TAG, "L'ID del gruppo è nullo")
            }
        }
    }


    
    private fun getDateString(timestampMillis: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Rome") // Imposta il fuso orario su Italia
        return dateFormat.format(Date(timestampMillis))
    }

    private fun updateChatGroup(memberId: String, groupId: String, message: MessageData, groupName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val chatUserRef = database.getReference("chats/$memberId/$groupId")

                    // Creo una mappa delle modifiche da apportare
                    val updates = HashMap<String, Any>()
                    updates["lastMessage"] = message.text
                    updates["timeStamp"] = message.timeStamp
                    updates["timestampMillis"] = message.timestampMillis

                    // Applica le modifiche solo ai campi specificati
                    chatUserRef.updateChildren(updates)
                }

        }



    private fun updateChatGroupCreate(memberId: String, groupId: String, message: MessageData, groupImageUrl: String, groupName: String, groupMemIds: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {

            Log.i("MEMBERS", "$groupMemIds")

            val chatUserRef = database.getReference("chats/$memberId/$groupId")
            val chatUser = ChatData(
                groupId,
                groupImageUrl,
                message.text,
                message.timeStamp,
                message.timestampMillis,
                groupName,
                true,
                groupMemIds
            )
            chatUserRef.setValue(chatUser)
        }
    }

    private fun updateGroupImage(memberId: String, groupId: String, messengerImageUrl: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {

            if (messengerImageUrl != null) {
                // Carica l'immagine nello storage di Firebase
                val storageRef =
                    FirebaseStorage.getInstance().reference.child("group_images/${messengerImageUrl.lastPathSegment}")
                val uploadTask = storageRef.putFile(messengerImageUrl)

                uploadTask.addOnSuccessListener {
                    // Ottieni l'URL dell'immagine caricata
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Ora hai l'URL dell'immagine, puoi aggiornare il database con questo URL
                        val chatUserRef = database.getReference("chats/$memberId/$groupId")
                        val imageUrl = uri.toString()
                        // Aggiorna il tuo database Realtime con imageUrl
                        chatUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Creo una mappa delle modifiche da apportare
                                val updates = HashMap<String, Any>()
                                updates["image"] = imageUrl

                                // Applica le modifiche solo ai campi specificati
                                chatUserRef.updateChildren(updates)

                                //da prendere nel groupFragment
                                imageString.postValue(messengerImageUrl.toString())
                            }

                            // Esegui altre operazioni necessarie, come la creazione del gruppo
                            // ...
                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                    }.addOnFailureListener { exception ->
                        // Gestisci l'errore nel caricamento dell'immagine
                        // ...
                    }
                }
            }
        }
    }


     fun updateGroupImageFromGroupDetails(groupId: String, messengerImageUrl: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {

            val currentList = groupUsersList.value ?: emptyList()
            if (messengerImageUrl != null) {
                // Carica l'immagine nello storage di Firebase
                val storageRef =
                    FirebaseStorage.getInstance().reference.child("group_images/${messengerImageUrl.lastPathSegment}")
                val uploadTask = storageRef.putFile(messengerImageUrl)

                uploadTask.addOnSuccessListener {
                    // Ottieni l'URL dell'immagine caricata
                    storageRef.downloadUrl.addOnSuccessListener { uri ->

                        for (userData in currentList) {
                        // Ora hai l'URL dell'immagine, puoi aggiornare il database con questo URL
                        val chatUserRef = database.getReference("chats/${userData.uid}/$groupId")
                        val imageUrl = uri.toString()
                        // Aggiorna il tuo database Realtime con imageUrl
                        chatUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Creo una mappa delle modifiche da apportare
                                val updates = HashMap<String, Any>()
                                updates["image"] = imageUrl

                                // Applica le modifiche solo ai campi specificati
                                chatUserRef.updateChildren(updates)

                                //da prendere nel groupDetailsFragment
                                imageStringDetails.postValue(messengerImageUrl.toString())
                            }

                            // Esegui altre operazioni necessarie, come la creazione del gruppo
                            // ...
                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                        }
                    }.addOnFailureListener { exception ->
                        // Gestisci l'errore nel caricamento dell'immagine
                        // ...
                    }
                }
            }
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


    fun retrieveUsersInGroup(messangerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser

            val chatsRef = database.getReference("chats/${user?.uid}/${messangerId}")

            // Listener per chat da user ai vari messenger
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Svuota la lista all'inizio del metodo
                    val updatedList = mutableListOf<UserData>()

                    val existingChatData = snapshot.getValue(ChatData::class.java)
                    val usersInGroup = existingChatData?.groupMembers ?: emptyList()

                    for (memberId in usersInGroup) {
                        fireStore.collection("users")
                            .document(memberId)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    // ... (resto del codice)
                                    val name = documentSnapshot.getString("name") ?: ""
                                    val age = documentSnapshot.getString("age") ?: ""
                                    val location = documentSnapshot.getString("location") ?: ""
                                    val image = documentSnapshot.getString("image") ?: ""
                                    val tag = documentSnapshot.getString("tag") ?: ""
                                    val tag2 = documentSnapshot.getString("tag2") ?: ""
                                    val tag3 = documentSnapshot.getString("tag3") ?: ""
                                    val stato = documentSnapshot.getString("stato") ?: ""
                                    val privatetag1 = documentSnapshot.getString("privatetag1") ?: ""
                                    val privatetag2 = documentSnapshot.getString("privatetag2") ?: ""
                                    val privatetag3 = documentSnapshot.getString("privatetag3") ?: ""
                                    val userData = UserData(uid=memberId,name=name,location=location,age=age,image=image,tag=tag,tag2=tag2,tag3=tag3, stato=stato,privatetag1=privatetag1,privatetag2=privatetag2,privatetag3=privatetag3)

                                    updatedList.add(userData)
                                    groupUsersList.postValue(updatedList)
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            }

            // Rimuovi il listener ValueEventListener se è già stato impostato in precedenza
            chatsRef.orderByChild("timestampMillis").removeEventListener(valueEventListener)
            chatsRef.orderByChild("timestampMillis").addValueEventListener(valueEventListener)
        }
    }

}
