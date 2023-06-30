package it.ter.sync.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.ter.sync.database.repository.UserRepository
import it.ter.sync.database.user.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.*

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val userRepository: UserRepository = UserRepository(application)

    var loginResult: MutableLiveData<Boolean> = MutableLiveData()
    var registrationResult: MutableLiveData<String> = MutableLiveData()
    var currentUser: MutableLiveData<UserData> = MutableLiveData()
    var userUpdated: MutableLiveData<Boolean> = MutableLiveData()
    var users: MutableLiveData<List<UserData>> = MutableLiveData()



    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        loginResult.postValue(true)
                    } else {
                        loginResult.postValue(false)
                    }
                }
        }
    }

    fun isUserLoggedIn() : Boolean{
        // Verifica se l'utente è autenticato
        val user = firebaseAuth.currentUser
        if (user != null) {
            return true
        }
        return false
    }

    fun getUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                currentUser.postValue(userRepository.getUserByUid(user.uid))
            }
        }
    }

    fun register(name: String, age: String, location: String, email: String, password: String, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        registrationResult.postValue("Success")
                        task.result.user?.let {
                            val userData = UserData(it.uid,name,email,location,age)

                            // Salva le informazioni su fireStore
                            addUserToFireStore(userData, latitude, longitude)

                            // Salva le informazioni dell'utente anche nel Database locale
                            viewModelScope.launch(Dispatchers.IO) {
                                userRepository.insertUser(userData)
                            }
                        }
                    } else {
                        // Si è verificato un errore durante la registrazione dell'utente
                        val exception = task.exception
                        registrationResult.postValue(exception?.message.toString())
                    }
                }
        }
    }

    private fun addUserToFireStore(userData: UserData, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {

            // Aggiungi le informazioni aggiuntive dell'utente nel database firestore
            val userAdditionalData = hashMapOf(
                "name" to userData.name,
                "age" to userData.age,
                "location" to userData.location,
                "latitude" to latitude,
                "longitude" to longitude
            )
            // Salva le informazioni aggiuntive dell'utente nel database Firestore
            fireStore.collection("users")
                .document(userData.uid)
                .set(userAdditionalData)
                .addOnSuccessListener {
                    Log.i(TAG, "User add to fireStore")
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, exception.message.toString())
                }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseAuth.getInstance().signOut()
            loginResult.postValue(false)
        }
    }

    fun updateUserInfo(name: String, age: String, tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                val userAdditionalData = hashMapOf<String, Any>(
                    "name" to name,
                    "age" to age,
                    "tag" to tag
                )

                fireStore.collection("users")
                    .document(user.uid)
                    .update(userAdditionalData)
                    .addOnSuccessListener {

                        userUpdated.postValue(true)

                        viewModelScope.launch(Dispatchers.IO) {
                            userRepository.updateUserInfo(user.uid, name, age, tag)
                        }
                    }
                    .addOnFailureListener { exception ->
                        userUpdated.postValue(false)
                        Log.i(TAG, exception.message.toString())
                    }

            }
        }
    }
    fun updateUserPosition(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                val userAdditionalData = hashMapOf<String, Any>(
                    "latitude" to latitude,
                    "longitude" to longitude
                )

                fireStore.collection("users")
                    .document(user.uid)
                    .update(userAdditionalData)
                    .addOnSuccessListener {
                        userUpdated.postValue(true)
                    }
                    .addOnFailureListener { exception ->
                        userUpdated.postValue(false)
                        Log.i(TAG, exception.message.toString())
                    }

            }
        }
    }


     fun updateUserImage(imageBitmap: Bitmap) {
        // Converti il bitmap in un array di byte
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

         // Converti l'array di byte in una stringa Base64
         val imageString = Base64.encodeToString(image, Base64.DEFAULT)

        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                val userAdditionalData = hashMapOf<String, Any>(
                    "image" to imageString,
                )

                fireStore.collection("users")
                    .document(user.uid)
                    .update(userAdditionalData)
                    .addOnSuccessListener {
                        userUpdated.postValue(true)
                    }
                    .addOnFailureListener { exception ->
                        userUpdated.postValue(false)
                        Log.i(TAG, exception.message.toString())
                    }

            }
        }

    }


    fun updateHome(currentLatitude: Double, currentLongitude: Double, tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateUserPosition(currentLatitude, currentLongitude)

            val user = firebaseAuth.currentUser
            fireStore.collection("users")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val userList = mutableListOf<UserData>()

                    for (document in querySnapshot) {
                        val uid = document.id
                        // se si tratta dell'utente loggato non mi interessa
                        if (uid != user?.uid) {
                            val name = document.getString("name") ?: ""
                            val age = document.getString("age") ?: ""
                            val location = document.getString("location") ?: ""
                            val image = document.getString("image") ?: ""
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val longitude = document.getDouble("longitude") ?: 0.0
                            val documentTag = document.getString("tag") ?: ""

                            val MAX_DISTANCE = 1000000000000000000.0 // in chilometri

                            val distance = calculateDistance(
                                latitude,
                                longitude,
                                currentLatitude,
                                currentLongitude
                            )

                            // Stampa la distanza nel log
                            Log.d("TAG", "Distanza = $distance")

                            val lowercaseTag = documentTag.toLowerCase()
                            val lowercaseSearchString = tag.toLowerCase()

                            if (distance <= MAX_DISTANCE && (lowercaseTag == lowercaseSearchString || lowercaseSearchString.isEmpty())) {
                                // Crea un oggetto User utilizzando i dati ottenuti dal documento
                                val user = UserData(
                                    uid = uid,
                                    name = name,
                                    location = location,
                                    age = age,
                                    image = image,
                                    tag = tag
                                )
                                userList.add(user)
                            }
                        }
                    }

                    users.postValue(userList)
                    Log.i(TAG, "GetAllUsers Success")
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, exception.message.toString())
                }
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double, // Coordinate del primo punto
        lat2: Double, lon2: Double  // Coordinate del secondo punto
    ): Double {
        val earthRadius = 6371 // Raggio medio della Terra in chilometri

        // Converti le coordinate in radianti
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // Calcola la differenza tra le latitudini e le longitudini
        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        // Applica la formula di Haversine
        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}