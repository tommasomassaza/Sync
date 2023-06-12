package it.ter.sync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val loginResult: MutableLiveData<Boolean> = MutableLiveData()
    val registrationResult: MutableLiveData<String> = MutableLiveData()

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

    fun register(name: String, age: String, place: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        user?.let {
                            // Aggiorna le informazioni aggiuntive dell'utente nel database
                            val userAdditionalData = hashMapOf(
                                "name" to name,
                                "age" to age,
                                "place" to place
                            )
                            // Salva le informazioni aggiuntive dell'utente nel database Firestore
                            fireStore.collection("users")
                                .document(user.uid)
                                .set(userAdditionalData)
                                .addOnSuccessListener {
                                    registrationResult.postValue("Success")
                                }
                                .addOnFailureListener { exception ->
                                    registrationResult.postValue(exception.message.toString())
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


    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseAuth.getInstance().signOut()
            loginResult.postValue(false)
        }
    }
}
