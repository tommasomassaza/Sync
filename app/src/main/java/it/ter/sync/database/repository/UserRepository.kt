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

    suspend fun insertUser(user: UserData) {
        dBUserDao.insert(user)
    }

    suspend fun getUserByUid(uid: String): UserData? {
        return dBUserDao.getUserByUid(uid)
    }

    suspend fun updateUserInfo(
        userId: String,
        name: String,
        age: String,
        tag: String,
        tag2: String,
        tag3: String,
        stato: String,
        privatetag1: String,
        privatetag2: String,
        privatetag3: String,
        timestampMillis: Long
    ) {
        dBUserDao.updateUserName(userId, name)
        dBUserDao.updateUserAge(userId, age)
        dBUserDao.updateUserTag(userId, tag)
        dBUserDao.updateUserTag2(userId, tag2)
        dBUserDao.updateUserTag3(userId, tag3)
        dBUserDao.updateUserStato(userId, stato)
        dBUserDao.updateUserPrivateTag1(userId, privatetag1)
        dBUserDao.updateUserPrivateTag2(userId, privatetag2)
        dBUserDao.updateUserPrivateTag3(userId, privatetag3)
        dBUserDao.updateUserTimestamp(userId, timestampMillis)
    }

    suspend fun updateUserImage(userId: String, imageUrl: String, timestampMillis: Long) {
        dBUserDao.updateUserImage(userId, imageUrl)
        dBUserDao.updateUserTimestamp(userId, timestampMillis)
    }

    suspend fun deleteUser(userId: String) {
        dBUserDao.deleteUser(userId)
    }
}