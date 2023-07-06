package it.ter.sync.database.notify

class NotificationData (
    val type: NotificationType = NotificationType.LIKE,
    var image: String = "",
    var text: String = "",
    var timeStamp: String = "",
    val timestampMillis: String = "0",
    var displayed: Boolean = false,
    var notifierId: String? = "",
    var notifierName: String? = ""
){}