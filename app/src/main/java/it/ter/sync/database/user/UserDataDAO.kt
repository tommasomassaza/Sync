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


    ///////////////////// update query /////////////////////
    @Update
    suspend fun updateUser(userData: UserData)


    ///////////////////// Deletion queries ////////////////// ///
    @Delete
    suspend fun delete(userData: UserData?)

    @Delete
    suspend fun deleteAll(vararg usersData: UserData?)
}