package it.ter.sync.database.message

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
class MessageData(
    @PrimaryKey()
    var uid: String = "",

    var text: String = "",
    val timestampMillis: Long = 0,
    var timeStamp: String = "",
    var senderId: String? = "",
    var receiverId: String? = ""
){}