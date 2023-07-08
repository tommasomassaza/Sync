package it.ter.sync.database.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDataDAO {
    ///////////////////// insertion queries /////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userData: UserData)


    ///////////////////// selection queries /////////////////////
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUid(uid: String): UserData?

    @Query("SELECT image FROM users WHERE uid = :uid")
    suspend fun getUserImage(uid: String): String?

    ///////////////////// update query /////////////////////
    @Query("UPDATE users SET name = :name WHERE uid = :userId")
    suspend fun updateUserName(userId: String, name: String)

    @Query("UPDATE users SET location = :location WHERE uid = :userId")
    suspend fun updateUserLocation(userId: String, location: String)

    @Query("UPDATE users SET age = :age WHERE uid = :userId")
    suspend fun updateUserAge(userId: String, age: String)

    @Query("UPDATE users SET image = :image WHERE uid = :userId")
    suspend fun updateUserImage(userId: String, image: String)

    @Query("UPDATE users SET tag = :tag WHERE uid = :userId")
    suspend fun updateUserTag(userId: String, tag: String)

    @Query("UPDATE users SET tag2 = :tag2 WHERE uid = :userId")
    suspend fun updateUserTag2(userId: String, tag2: String)

    @Query("UPDATE users SET tag3 = :tag3 WHERE uid = :userId")
    suspend fun updateUserTag3(userId: String, tag3: String)

    @Query("UPDATE users SET timestampMillis = :timestampMillis WHERE uid = :userId")
    abstract fun updateUserTimestamp(userId: String, timestampMillis: Long)

    ///////////////////// Deletion queries /////////////////////
    @Query("DELETE FROM users WHERE uid = :userId")
    suspend fun deleteUser(userId: String)
}