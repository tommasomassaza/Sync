package it.ter.sync.database.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
open class UserData(
    var name: String = "",
    var location: String = "",
    var age: String = ""
) {
    @PrimaryKey()
    var email: String = ""
}