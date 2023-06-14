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

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private var TAG = this::class.simpleName
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val users: MutableLiveData<List<UserData>> = MutableLiveData()


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

                            // Crea un oggetto User utilizzando i dati ottenuti dal documento
                            val user = UserData(uid = uid, name = name, location = location, age = age)
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