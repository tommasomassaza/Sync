package it.ter.sync.database.notify

class NotificationData (
    val type: NotificationType = NotificationType.LIKE,
    var text: String = "",
    var timeStamp: String = "",
    val timestampMillis: Long = 0,
    var displayed: Boolean = false,
    var userId: String? = "",
    var userName: String? = ""
){}