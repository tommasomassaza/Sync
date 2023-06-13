package it.ter.sync.database.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
open class UserData(
    @PrimaryKey()
    var uid: String,

    var name: String,
    var email: String,
    var location: String,
    var age: String
) {}