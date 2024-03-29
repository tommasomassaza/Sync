package it.ter.sync.database.message

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "messages")
class MessageData(
    @PrimaryKey()
    var uid: String = "",

    var image: String = "",
    var text: String = "",
    val timestampMillis: String = "0",
    var timeStamp: String = "",
    var senderId: String? = "",
    var receiverId: String? = "",
    var senderName: String? = ""

){}