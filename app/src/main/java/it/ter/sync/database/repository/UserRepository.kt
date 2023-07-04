package it.ter.sync.database.repository

import android.content.Context
import androidx.lifecycle.ViewModel
import it.ter.sync.database.MyRoomDatabase
import it.ter.sync.database.user.UserData
import it.ter.sync.database.user.UserDataDAO

class UserRepository (application: Context?) : ViewModel() {
    private val dBUserDao: UserDataDAO

    init {
        val db = MyRoomDatabase.getDatabase(application!!)
        dBUserDao = db.myUserDataDao()!!
    }

    /**
     * Se rowsInserted è diverso da null, viene restituito il suo valore.
     * Altrimenti, viene restituito il valore di fallback 0.
     */
    suspend fun insertUser(user: UserData) {
        dBUserDao.insert(user)
    }


    suspend fun getUserByUid(uid: String): UserData? {
        return dBUserDao.getUserByUid(uid)
    }

    suspend fun getUserImage(uid: String): String? {
        return dBUserDao.getUserImage(uid)
    }

    suspend fun updateUserInfo(userId: String, name: String, age: String, tag: String, tag2: String, tag3: String) {
        dBUserDao.updateUserName(userId, name)
        dBUserDao.updateUserAge(userId, age)
        dBUserDao.updateUserTag(userId, tag)
        dBUserDao.updateUserTag2(userId, tag2)
        dBUserDao.updateUserTag3(userId, tag3)
    }

    suspend fun updateUserImage(userId: String, imageUrl: String) {
        dBUserDao.updateUserImage(userId, imageUrl)
    }
}