package it.ter.sync.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.ter.sync.database.repository.UserRepository
import it.ter.sync.database.user.UserData
import it.ter.sync.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.*

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val userRepository: UserRepository = UserRepository(application)

    var loginResult: MutableLiveData<Boolean> = MutableLiveData()
    var registrationResult: MutableLiveData<String> = MutableLiveData()
    var currentUser: MutableLiveData<UserData?> = MutableLiveData()
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
                val userData = userRepository.getUserByUid(user.uid)
                if (userData != null && userData.image != "") { // è presente in locale
                    currentUser.postValue(userData)
                } else {
                    fireStore.collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val name = documentSnapshot.getString("name") ?: ""
                                val age = documentSnapshot.getString("age") ?: ""
                                val location = documentSnapshot.getString("location") ?: ""
                                val imageUrl = documentSnapshot.getString("image") ?: ""
                                val email = user.email ?: ""
                                val userData = UserData(user.uid, name, email, location, age, imageUrl)
                                currentUser.postValue(userData)

                                // Salva le informazioni dell'utente anche nel Database locale
                                viewModelScope.launch(Dispatchers.IO) {
                                    userRepository.insertUser(userData)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Errore durante il recupero delle informazioni dell'utente da Firestore: ${exception.message}")
                        }
                }
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

    fun updateUserInfo(name: String, age: String, tag: String, tag2: String, tag3: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                val userAdditionalData = hashMapOf<String, Any>(
                    "name" to name,
                    "age" to age,
                    "tag" to tag,
                    "tag2" to tag2,
                    "tag3" to tag3
                )

                fireStore.collection("users")
                    .document(user.uid)
                    .update(userAdditionalData)
                    .addOnSuccessListener {

                        userUpdated.postValue(true)

                        viewModelScope.launch(Dispatchers.IO) {
                            userRepository.updateUserInfo(user.uid, name, age, tag, tag2, tag3)
                        }
                    }
                    .addOnFailureListener { exception ->
                        userUpdated.postValue(false)
                        Log.i(TAG, exception.message.toString())
                    }

            }
        }
    }
    private fun updateUserPosition(latitude: Double, longitude: Double) {
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

    fun updateUserImage(imageUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                val storageRef = storage.reference.child("images/${user.uid}.jpg")
                val uploadTask = storageRef.putFile(imageUri!!)

                uploadTask.addOnSuccessListener {
                    Log.e(TAG, "Immagine aggiunta con successo.")
                    storageRef.downloadUrl.addOnSuccessListener {
                        val userAdditionalData = hashMapOf<String, Any>(
                            "image" to it.toString(),
                        )

                        val imageUrl = it
                        fireStore.collection("users")
                            .document(user.uid)
                            .update(userAdditionalData)
                            .addOnSuccessListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    userRepository.updateUserImage(user.uid, imageUrl.toString())
                                    getUserInfo()
                                }
                            }
                            .addOnFailureListener { exception ->
                                userUpdated.postValue(false)
                                Log.i(TAG, exception.message.toString())
                            }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error uploading image to Firebase Storage: ${exception.message}")
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
                            val userData = document.toObject(UserData::class.java)
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val longitude = document.getDouble("longitude") ?: 0.0

                            val MAX_DISTANCE = 1000000000000000000.0 // in chilometri

                            val distance = Utils.calculateDistance(latitude,longitude,currentLatitude,currentLongitude)

                            // Stampa la distanza nel log
                            Log.d("TAG", "Distanza = $distance")

                            val lowercaseSearchString = tag.toLowerCase()

                            val lowercaseTag = userData.tag.toLowerCase()
                            val lowercaseTag2 = userData.tag2.toLowerCase()
                            val lowercaseTag3 = userData.tag3.toLowerCase()

                            if (distance <= MAX_DISTANCE && (lowercaseTag == lowercaseSearchString || lowercaseSearchString.isEmpty() ||lowercaseTag2 == lowercaseSearchString
                                        || lowercaseTag3 == lowercaseSearchString || userData.name == lowercaseSearchString)) {

                                // Crea un oggetto User utilizzando i dati ottenuti dal documento
                                val user = UserData(
                                    uid = uid,
                                    name = userData.name,
                                    location = userData.location,
                                    age = Utils.calculateAge(userData.age).toString(),
                                    image = userData.image,
                                    tag = userData.tag,
                                    tag2 = userData.tag2,
                                    tag3 = userData.tag3
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

    fun setCoordinates(latitude: Double, longitude: Double) {

    }
}