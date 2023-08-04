package it.ter.sync.database.chat

import androidx.room.Entity
import androidx.room.TypeConverters
import it.ter.sync.database.message.RoomTypeConverters

@Entity(tableName = "chats")
@TypeConverters(RoomTypeConverters::class)
class ChatData (
    var uid: String = "",

    var image: String = "",
    var lastMessage: String = "",
    var timeStamp: String = "",
    val timestampMillis: String = "0",
    var messengerName: String? = "",
    var messengerId: String? = "",

    // Lista di stringhe
    var groupIDs: List<String> = emptyList()

){}