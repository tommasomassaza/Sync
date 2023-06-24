package it.ter.sync.database.repository

import android.content.Context
import androidx.lifecycle.ViewModel
import it.ter.sync.database.MyRoomDatabase
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.message.MessageDataDAO

class MessageRepository (application: Context?) : ViewModel() {
    private val dBMessageDao: MessageDataDAO

    init {
        val db = MyRoomDatabase.getDatabase(application!!)
        dBMessageDao = db.myMessageDataDao()!!
    }

    suspend fun insertMessage(message: MessageData) {
        dBMessageDao.insert(message)
    }

    fun getMessagesBySenderAndReceiver(userId: String, messengerId: String): List<MessageData>? {
        return dBMessageDao.getMessagesBySenderAndReceiver(userId, messengerId)
    }

    fun getLastMessage(userId: String, messengerId: String): MessageData? {
        return dBMessageDao.getLastMessage(userId, messengerId)
    }
}