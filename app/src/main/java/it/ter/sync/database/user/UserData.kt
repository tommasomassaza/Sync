package it.ter.sync.database.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "users")
open class UserData(
    @PrimaryKey()
    var uid: String = "",

    var name: String = "",
    var email: String = "",
    var location: String = "",
    var age: String = "",
    var image: String = "",
    var tag: String = "",
    var tag2: String = "",
    var tag3: String = "",
    val timestampMillis: Long = 0
) {}