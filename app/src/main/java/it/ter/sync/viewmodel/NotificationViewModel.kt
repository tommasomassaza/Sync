package it.ter.sync.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.notify.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private var ref: DatabaseReference? = null
    private var refNotDisplayed: DatabaseReference? = null

    // List of uid
    var notificationList: MutableLiveData<List<NotificationData>> = MutableLiveData()
    var notificationListNotDisplayed: MutableLiveData<List<NotificationData>> = MutableLiveData()

    var likeList: MutableLiveData<List<String>> = MutableLiveData()


    private val valueEventListenerNotDisplayed = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val notifications: MutableList<NotificationData> = mutableListOf()
            for (userSnapshot in snapshot.children) {
                for (notificationSnapshot in userSnapshot.children) {
                    val notification = notificationSnapshot.getValue(NotificationData::class.java)
                    notification?.let {
                        if (!it.displayed) {
                            notifications.add(it)
                        }
                    }
                }
            }
            notificationListNotDisplayed.postValue(notifications)
        }
        override fun onCancelled(error: DatabaseError) {
            // Gestisci l'errore di recupero dei messaggi
            Log.i(TAG, error.message)
        }
    }

    fun retrieveNotificationsNotDisplayed() {
        viewModelScope.launch(Dispatchers.IO) {
            // Rimuovi il listener precedente se presente
            refNotDisplayed?.removeEventListener(valueEventListenerNotDisplayed)

            val user = firebaseAuth.currentUser
            refNotDisplayed = database.getReference("notifications/${user?.uid}")

            refNotDisplayed!!.addValueEventListener(valueEventListenerNotDisplayed)
        }
    }

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val notifications: MutableList<NotificationData> = mutableListOf()
            for (userSnapshot in snapshot.children) {
                for (notificationSnapshot in userSnapshot.children) {
                    val notification = notificationSnapshot.getValue(NotificationData::class.java)
                    notification?.let {
                        notifications.add(it)
                    }
                }
            }
            notificationList.postValue(notifications)
        }
        override fun onCancelled(error: DatabaseError) {
            // Gestisci l'errore di recupero dei messaggi
            Log.i(TAG, error.message)
        }
    }

    fun retrieveNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            // Rimuovi il listener precedente se presente
            ref?.removeEventListener(valueEventListener)

            val user = firebaseAuth.currentUser
            ref = database.getReference("notifications/${user?.uid}")

            ref!!.orderByChild("timestampMillis").addValueEventListener(valueEventListener)
        }
    }

    private val valueEventListenerLike = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val likes: MutableList<String> = mutableListOf()
            for (userSnapshot in snapshot.children) {
                for (likeSnapshot in userSnapshot.children) {
                    val like = likeSnapshot.getValue(String::class.java)
                    like?.let {
                        likes.add(like)
                    }
                }
            }
            likeList.postValue(likes)
        }
        override fun onCancelled(error: DatabaseError) {
            // Gestisci l'errore di recupero dei messaggi
            Log.i(TAG, error.message)
        }
    }
    fun retrieveLikes() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser

            val likeRef = database.getReference("likes/${user?.uid}")
            likeRef.addValueEventListener(valueEventListenerLike)
        }
    }

    fun addLikeNotification(userId: String, notifierName: String, image: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${userId}/${user?.uid}/like")

            val timestampMillis = System.currentTimeMillis()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateFormat.timeZone =
                TimeZone.getTimeZone("Europe/Rome") // Imposta il fuso orario su Italia
            val dateString = dateFormat.format(Date(timestampMillis))

            val notification = NotificationData(NotificationType.LIKE,image,"",dateString,timestampMillis.toString(),false,user?.uid,notifierName)

            ref.setValue(notification)
                .addOnSuccessListener {
                    Log.i(TAG, "Notifica inviata con successo")

                    val likeRef = database.getReference("likes/${user?.uid}/${userId}")
                    likeRef.child(userId).setValue(userId)
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "${error.message}")
                }
        }
    }

    fun notificationsDisplayed() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${user?.uid}")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        for (notificationSnapshot in userSnapshot.children) {
                            val notification = notificationSnapshot.getValue(NotificationData::class.java)
                            val notificationRef = notificationSnapshot.ref

                            if (notification?.displayed == false) {
                                // Aggiorna il campo displayed a true solo se è impostato a false
                                notificationRef.child("displayed").setValue(true)
                                    .addOnSuccessListener {
                                        Log.i(TAG, "Campo displayed aggiornato con successo")
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e(TAG, "${error.message}")
                                    }
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, error.message)
                }
            })
        }
    }

    fun remove(notification: NotificationData) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${user?.uid}/${notification.notifierId}/${notification.type.toString().lowercase()}")

            ref.removeValue()
                .addOnSuccessListener {
                    Log.i(TAG, "Notifica rimossa con successo")
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "${error.message}")
                }
        }
    }

    fun add(notification: NotificationData) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${user?.uid}/${notification.notifierId}/${notification.type.toString().lowercase()}")

            ref.setValue(notification)
                .addOnSuccessListener {
                    Log.i(TAG, "Notifica aggiunta con successo")
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "${error.message}")
                }
        }
    }

    fun deleteLikeNotification(userId: String) {

        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${userId}/${user?.uid}/like")

            ref.removeValue()
                .addOnSuccessListener {
                    Log.i(TAG, "Notifica eliminata con successo")

                    val likeRef = database.getReference("likes/${user?.uid}/${userId}")
                    likeRef.removeValue()
                        .addOnSuccessListener {
                            Log.i(TAG, "Like eliminato con successo")
                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "${error.message}")
                        }
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "${error.message}")
                }
        }
    }
}