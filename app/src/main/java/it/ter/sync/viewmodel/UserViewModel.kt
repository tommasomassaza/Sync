package it.ter.sync.viewmodel

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.repository.UserRepository
import it.ter.sync.database.user.UserData
import it.ter.sync.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


class UserViewModel(private val application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val userRepository: UserRepository = UserRepository(application)

    var loginResult: MutableLiveData<Boolean> = MutableLiveData()
    var registrationResult: MutableLiveData<String> = MutableLiveData()
    var currentUser: MutableLiveData<UserData?> = MutableLiveData()
    val accountFriend: MutableLiveData<UserData?> = MutableLiveData()
    var userUpdated: MutableLiveData<Boolean> = MutableLiveData()
    var users: MutableLiveData<List<UserData>> = MutableLiveData()


    var chatList: MutableLiveData<List<ChatData>> = MutableLiveData()

    // Distanza di default
    private var maxDistance: Double = 50.0
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private var searchStrings: ArrayList<String> = ArrayList()

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if(email.isNullOrEmpty() || password.isNullOrEmpty()) {
                loginResult.postValue(false)
                return@launch
            }
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
                if (userData != null) { // è presente in locale
                    currentUser.postValue(userData)
                }
                // Controllo anche su firestore se l'utente è stato modificato
                fireStore.collection("users")
                    .document(user.uid)
                    .addSnapshotListener { documentSnapshot, exception ->
                        if (exception != null) {
                            Log.e(TAG, "Errore durante il recupero delle informazioni dell'utente da Firestore: ${exception.message}")
                            return@addSnapshotListener
                        }
                        if (documentSnapshot != null) {
                            val timestampLastModified = documentSnapshot.getLong("timestampMillis") ?: 0
                            if(timestampLastModified > (userData?.timestampMillis ?: 0)) {
                                val name = documentSnapshot.getString("name") ?: ""
                                val age = documentSnapshot.getString("age") ?: ""
                                val location = documentSnapshot.getString("location") ?: ""
                                val image = documentSnapshot.getString("image") ?: ""
                                val email = user.email ?: ""
                                val tag = documentSnapshot.getString("tag") ?: ""
                                val tag2 = documentSnapshot.getString("tag2") ?: ""
                                val tag3 = documentSnapshot.getString("tag3") ?: ""
                                val stato = documentSnapshot.getString("stato") ?: ""
                                val privatetag1 = documentSnapshot.getString("privatetag1") ?: ""
                                val privatetag2 = documentSnapshot.getString("privatetag2") ?: ""
                                val privatetag3 = documentSnapshot.getString("privatetag3") ?: ""
                                val userDataFireStore = UserData(user.uid,name,email,location,age,image,tag,tag2,tag3,stato, privatetag1, privatetag2, privatetag3, timestampLastModified)

                                currentUser.postValue(userDataFireStore)

                                // Salva le informazioni dell'utente anche nel Database locale
                                viewModelScope.launch(Dispatchers.IO) {
                                    userRepository.insertUser(userDataFireStore)
                                }
                            }
                        }
                    }
            }
        }
    }

    fun getUserInfo(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
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
                        val userData = UserData(uid=userId,name=name,location=location,age=age,image=image,tag=tag,tag2=tag2,tag3=tag3, stato=stato,privatetag1=privatetag1,privatetag2=privatetag2,privatetag3=privatetag3)

                        accountFriend.postValue(userData)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Errore durante il recupero delle informazioni dell'utente da Firestore: ${exception.message}")
                }
        }
    }


    fun register(name: String, age: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    registrationResult.postValue("Success")
                    task.result.user?.let {
                        val userData = UserData(uid=it.uid,name=name,email=email,age=age)

                        // Salva le informazioni su fireStore
                        addUserToFireStore(userData)
                    }
                }
                .addOnFailureListener {
                    Log.i(TAG, it.message.toString())
                }
        }
    }

    private fun addUserToFireStore(userData: UserData) {
        viewModelScope.launch(Dispatchers.IO) {

            val timestampMillis = System.currentTimeMillis()

            // Aggiungi le informazioni aggiuntive dell'utente nel database firestore
            val userAdditionalData = hashMapOf(
                "name" to userData.name,
                "age" to userData.age,
                "location" to userData.location,
                "timestampMillis" to timestampMillis,
                "latitude" to 0.0,
                "longitude" to 0.0
            )
            // Salva le informazioni aggiuntive dell'utente nel database Firestore
            fireStore.collection("users")
                .document(userData.uid)
                .set(userAdditionalData)
                .addOnSuccessListener {
                    Log.i(TAG, "User add to fireStore")
                }
                .addOnFailureListener {
                    Log.i(TAG, it.message.toString())
                }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseAuth.getInstance().signOut()
            loginResult.postValue(false)
        }
    }

    fun updateUserInfo(name: String, age: String, tag: String, tag2: String, tag3: String, stato: String, privatetag1: String, privatetag2: String, privatetag3: String ) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestampMillis = System.currentTimeMillis()
            val user = firebaseAuth.currentUser
            user?.let {
                val userAdditionalData = hashMapOf<String, Any>(
                    "name" to name,
                    "age" to age,
                    "timestampMillis" to timestampMillis,
                    "tag" to tag,
                    "tag2" to tag2,
                    "tag3" to tag3,
                    "stato" to stato,
                    "privatetag1" to privatetag1,
                    "privatetag2" to privatetag1,
                    "privatetag3" to privatetag1
                )
                fireStore.collection("users")
                    .document(user.uid)
                    .update(userAdditionalData)
                    .addOnSuccessListener {
                        userUpdated.postValue(true)

                        viewModelScope.launch(Dispatchers.IO) {
                            userRepository.updateUserInfo(user.uid, name, age, tag, tag2, tag3, stato, privatetag1, privatetag2, privatetag3, timestampMillis)
                        }
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
            val timestampMillis = System.currentTimeMillis()
            val user = firebaseAuth.currentUser
            user?.let {
                val storageRef = storage.reference.child("images/${user.uid}.jpg")
                val uploadTask = storageRef.putFile(imageUri!!)

                uploadTask.addOnSuccessListener {
                    Log.e(TAG, "Immagine aggiunta nello storage.")
                    storageRef.downloadUrl.addOnSuccessListener {
                        val userAdditionalData = hashMapOf<String, Any>(
                            "image" to it.toString(),
                            "timestampMillis" to timestampMillis
                        )
                        val imageUrl = it
                        fireStore.collection("users")
                            .document(user.uid)
                            .update(userAdditionalData)
                            .addOnSuccessListener {
                                Log.e(TAG, "URL immagine aggiunto in firestore.")
                                viewModelScope.launch(Dispatchers.IO) {
                                    userRepository.updateUserImage(user.uid, imageUrl.toString(), timestampMillis)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.i(TAG, exception.message.toString())
                            }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error uploading image to Firebase Storage: ${exception.message}")
                }
            }
        }

    }

    fun deleteAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                user.delete()
                    .addOnSuccessListener {
                        fireStore.collection("users")
                            .document(user.uid)
                            .delete()
                            .addOnSuccessListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    // Rimuovi l'utente dal Database locale
                                    userRepository.deleteUser(user.uid)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG,"Errore durante l'eliminazione dell'utente da Firestore: ${exception.message}")
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG,"Errore durante l'eliminazione dell'utente: ${exception.message}")
                    }

            }
        }
    }

    private fun getNearestCity(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(application.applicationContext, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            return address.locality
        }
        return null
    }

    fun updateUserPosition(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            currentLatitude = latitude
            currentLongitude = longitude

            refreshRetrieveUser()

            val user = firebaseAuth.currentUser
            val nearestCity = getNearestCity(latitude,longitude)
            user?.let {
                val userAdditionalData = hashMapOf<String, Any>(
                    "location" to nearestCity.toString(),
                    "latitude" to latitude,
                    "longitude" to longitude
                )

                fireStore.collection("users")
                    .document(user.uid)
                    .update(userAdditionalData)
            }
        }
    }
    private fun update(querySnapshot: QuerySnapshot) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser

            val filteredUsers = querySnapshot.documents.filter { document ->
                val uid = document.id
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val tag = document.getString("tag") ?: ""
                val tag2 = document.getString("tag2") ?: ""
                val tag3 = document.getString("tag3") ?: ""
                val privatetag1 = document.getString("privatetag1") ?: ""
                val privatetag2 = document.getString("privatetag2") ?: ""
                val privatetag3 = document.getString("privatetag3") ?: ""

                val distance = Utils.calculateDistance(
                    latitude,
                    longitude,
                    currentLatitude,
                    currentLongitude
                )

                val hasMatchingTags = if (searchStrings.isNotEmpty()) {
                    val userTags = listOf(tag, tag2, tag3, privatetag1, privatetag2, privatetag3)
                    searchStrings.any { searchString ->
                        userTags.any { userTag ->
                            userTag.contains(searchString, ignoreCase = true)
                        }
                    }
                } else {
                    true
                }

                distance <= maxDistance && uid != user?.uid && hasMatchingTags
            }

            val userList = filteredUsers.mapNotNull { document ->
                val uid = document.id
                val userData = document.toObject(UserData::class.java)

                // Crea un oggetto UserData utilizzando i dati ottenuti dal documento
                userData?.let {
                    UserData(
                        uid = uid,
                        name = it.name,
                        location = it.location,
                        age = Utils.calculateAge(it.age).toString(),
                        image = it.image,
                        tag = it.tag,
                        tag2 = it.tag2,
                        tag3 = it.tag3
                    )
                }
            }

            users.postValue(userList)
            Log.i(TAG, "GetAllUsers Success")
        }
    }
    private fun refreshRetrieveUser(){
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("users")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    update(querySnapshot)
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, exception.message.toString())
                }
        }
    }
    fun retrieveUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("users")
                .addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        // Gestisci l'errore
                        Log.i(TAG, exception.message.toString())
                        return@addSnapshotListener
                    }
                    if (querySnapshot != null) {
                        update(querySnapshot)
                    }
                }
        }
    }

    fun updateMaxDistance(kmNumber: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            maxDistance = kmNumber
            refreshRetrieveUser()
        }
    }

    fun addTag(tag: String) {
        searchStrings.add(tag.lowercase(Locale.getDefault()))
        refreshRetrieveUser()
    }

    fun removeTag(tag: String) {
        searchStrings.remove(tag.lowercase(Locale.getDefault()))
        refreshRetrieveUser()
    }

    fun retrieveUsersWhoiHadAChatWith() {
        }



}