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

    val loginResult: MutableLiveData<Boolean> = MutableLiveData()
    val registrationResult: MutableLiveData<String> = MutableLiveData()
    val currentUser: MutableLiveData<UserData> = MutableLiveData()
    val userUpdated: MutableLiveData<Boolean> = MutableLiveData()

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

    fun register(name: String, age: String, location: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        registrationResult.postValue("Success")
                        viewModelScope.launch(Dispatchers.IO) {
                            task.result.user?.let { addUserToFireStore(it.uid,name,age,location,email) }
                        }
                    } else {
                        // Si è verificato un errore durante la registrazione dell'utente
                        val exception = task.exception
                        registrationResult.postValue(exception?.message.toString())
                    }
                }
        }
    }

    private suspend fun addUserToFireStore(uid: String, name: String, age: String, location: String, email: String){

        // Aggiungi le informazioni aggiuntive dell'utente nel database firestore
        val userAdditionalData = hashMapOf(
            "name" to name,
            "age" to age,
            "location" to location
        )
        // Salva le informazioni aggiuntive dell'utente nel database Firestore
        fireStore.collection("users")
            .document(uid)
            .set(userAdditionalData)
            .addOnSuccessListener {

                // Salva le informazioni dell'utente anche nel Database locale
                val userData = UserData(uid,name,email,location,age)
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertUser(userData)
                }
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, exception.message.toString())
            }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseAuth.getInstance().signOut()
            loginResult.postValue(false)
        }
    }

    fun updateUser(name: String, age: String, location: String) {
        val user = firebaseAuth.currentUser
        user?.let {

            val userAdditionalData = hashMapOf<String,Any>(
                "name" to name,
                "age" to age,
                "location" to location
            )

            fireStore.collection("users")
                .document(user.uid)
                .update(userAdditionalData)
                .addOnSuccessListener {

                    // Aggiorna le informazioni dell'utente anche nel Database locale
                    val userData = UserData(user.uid,name,user.email!!,location,age)
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            repository.updateUser(userData)
                            userUpdated.postValue(true)
                        } catch (e: Exception) {
                            userUpdated.postValue(true)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, exception.message.toString())
                }

        }
    }
}
