package it.ter.sync.database.message

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

    ///////////////////// update query /////////////////////


    ///////////////////// Deletion queries /////////////////////
}