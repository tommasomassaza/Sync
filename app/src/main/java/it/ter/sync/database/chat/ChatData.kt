package it.ter.sync.database.chat

class ChatData (
    var uid: String = "",

    var image: String = "",
    var lastMessage: String = "",
    var timeStamp: String = "",
    val timestampMillis: String = "0",
    var messengerName: String? = ""
){}