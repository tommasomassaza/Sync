package it.ter.sync.database.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import it.ter.sync.database.user.UserData

@Dao
interface MessageDataDAO {
    ///////////////////// insertion queries /////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageData: MessageData)


    ///////////////////// selection queries /////////////////////
    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :messengerId) OR (receiverId = :userId AND senderId = :messengerId) ORDER BY timestampMillis ASC")
    fun getMessagesBySenderAndReceiver(userId: String, messengerId: String): List<MessageData>?


    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :messengerId) OR (receiverId = :userId AND senderId = :messengerId) ORDER BY timestampMillis DESC LIMIT 1")
    fun getLastMessage(userId: String, messengerId: String): MessageData?

    ///////////////////// update query /////////////////////


    ///////////////////// Deletion queries /////////////////////
    @Query("DELETE FROM messages WHERE uid = :messageId")
    suspend fun deleteMessageById(messageId: String)
}