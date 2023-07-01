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
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.notify.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // List of uid
    var notificationList: MutableLiveData<List<NotificationData>> = MutableLiveData()
    var notificationListNotDisplayed: MutableLiveData<List<NotificationData>> = MutableLiveData()

    fun retrieveNotifications(displayed : Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${user?.uid}")

            // Listener per messaggi da user a messenger
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications: MutableList<NotificationData> = mutableListOf()
                    for (userSnapshot in snapshot.children) {
                        for (notificationSnapshot in userSnapshot.children) {

                            val notification =
                                notificationSnapshot.getValue(NotificationData::class.java)
                            notification?.let {
                                if ((!displayed && !it.displayed) || displayed) {
                                    notifications.add(it)
                                }
                            }
                        }

                    }
                    if(displayed) notificationList.postValue(notifications)
                    else notificationListNotDisplayed.postValue(notifications)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore di recupero dei messaggi
                    Log.i(TAG, error.message)
                }
            })
        }
    }

    fun addLikeNotification(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            val ref = database.getReference("notifications/${userId}/${user?.uid}/like")

            val timestampMillis = System.currentTimeMillis()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateFormat.timeZone =
                TimeZone.getTimeZone("Europe/Rome") // Imposta il fuso orario su Italia
            val dateString = dateFormat.format(Date(timestampMillis))

            val notification = NotificationData(
                NotificationType.LIKE,
                "",
                dateString,
                timestampMillis,
                false,
                userId
            )

            ref.setValue(notification)
                .addOnSuccessListener {
                    Log.i(TAG, "Notifica inviata con successo")
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

    fun remove(uid: String) {

    }
}