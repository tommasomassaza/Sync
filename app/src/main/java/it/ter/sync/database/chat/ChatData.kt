package it.ter.sync.database.chat

import androidx.room.Entity
import androidx.room.TypeConverters
import it.ter.sync.database.message.RoomTypeConverters

@Entity(tableName = "chats")
@TypeConverters(RoomTypeConverters::class)
class ChatData (
var uid: String = "", // Può essere l'ID dell'utente singolo o dell'ID del gruppo
var image: String = "",
var lastMessage: String = "",
var timeStamp: String = "",
val timestampMillis: String = "0",
var messengerName: String? = "",
var group: Boolean = false, // Aggiungiamo un campo booleano per identificare se è un gruppo o una chat singola
var groupMembers: List<String>? = null // Lista degli ID degli utenti partecipanti al gruppo (solo se è un gruppo)
){}
