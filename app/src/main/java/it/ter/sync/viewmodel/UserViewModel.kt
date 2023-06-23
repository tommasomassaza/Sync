package it.ter.sync.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.ter.sync.database.Repository
import it.ter.sync.database.user.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val repository: Repository = Repository(application)

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
                currentUser.postValue(repository.getUserByUid(user.uid))
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
                            val userData = UserData(it.uid,name,email,location,age, latitude, longitude)

                            // Salva le informazioni su fireStore
                            addUserToFireStore(userData)

                            // Salva le informazioni dell'utente anche nel Database locale
                            viewModelScope.launch(Dispatchers.IO) {
                                repository.insertUser(userData)
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

    private fun addUserToFireStore(userData: UserData) {
        viewModelScope.launch(Dispatchers.IO) {

            // Aggiungi le informazioni aggiuntive dell'utente nel database firestore
            val userAdditionalData = hashMapOf(
                "name" to userData.name,
                "age" to userData.age,
                "location" to userData.location,
                "latitude" to userData.latitude,
                "longitude" to userData.longitude
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

    fun updateUser(name: String, age: String, location: String, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            user?.let {
                val userAdditionalData = hashMapOf<String, Any>(
                    "name" to name,
                    "age" to age,
                    "location" to location,
                    "latitude" to latitude,
                    "longitude" to longitude
                )

                fireStore.collection("users")
                    .document(user.uid)
                    .update(userAdditionalData)
                    .addOnSuccessListener {

                        userUpdated.postValue(true)

                        // Aggiorna le informazioni dell'utente anche nel Database locale
                        val userData = UserData(user.uid, name, user.email!!, location, age, latitude, longitude)
                        viewModelScope.launch(Dispatchers.IO) {
                            repository.updateUser(userData)
                        }
                    }
                    .addOnFailureListener { exception ->
                        userUpdated.postValue(false)
                        Log.i(TAG, exception.message.toString())
                    }

            }
        }
    }

    fun getAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            fireStore.collection("users")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val userList = mutableListOf<UserData>()

                    for (document in querySnapshot) {
                        val uid = document.id
                        // se si tratta dell'utente loggato non mi interessa
                        if(uid != user?.uid) {
                            val name = document.getString("name") ?: ""
                            val age = document.getString("age") ?: ""
                            val location = document.getString("location") ?: ""
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val longitude = document.getDouble("longitude") ?: 0.0


                            // Crea un oggetto User utilizzando i dati ottenuti dal documento
                            val user = UserData(uid = uid, name = name, location = location, age = age, latitude = latitude, longitude = longitude)
                            userList.add(user)
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
}