package it.ter.sync.database

import android.content.Context
import androidx.lifecycle.ViewModel
import it.ter.sync.database.user.UserData
import it.ter.sync.database.user.UserDataDAO

class Repository (application: Context?) : ViewModel() {
    private val dBUserDao: UserDataDAO

    init {
        val db = MyRoomDatabase.getDatabase(application!!)
        dBUserDao = db.myUserDataDao()!!
    }

    /**
     * Se rowsInserted è diverso da null, viene restituito il suo valore.
     * Altrimenti, viene restituito il valore di fallback 0.
     */
    suspend fun insertUser(user: UserData): Boolean{
        val rowsInserted = dBUserDao.insert(user)
        return rowsInserted > 0
    }

    suspend fun existsUser(email: String): Boolean {
        return dBUserDao.existsUser(email)
    }

    suspend fun getUserByEmail(email: String): UserData? {
        return dBUserDao.getUserByEmail(email)
    }
}