package it.ter.sync.database.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDataDAO {
    ///////////////////// insertion queries /////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userData: UserData?): Long


    ///////////////////// selection queries /////////////////////
    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): UserData?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email LIMIT 1)")
    fun existsUser(email: String): Boolean


    ///////////////////// Deletion queries ////////////////// ///
    @Delete
    fun delete(userData: UserData?)

    @Delete
    fun deleteAll(vararg usersData: UserData?)
}