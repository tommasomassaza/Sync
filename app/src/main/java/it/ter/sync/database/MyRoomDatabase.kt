package it.ter.sync.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.ter.sync.database.message.MessageData
import it.ter.sync.database.message.MessageDataDAO
import it.ter.sync.database.user.UserData
import it.ter.sync.database.user.UserDataDAO

@Database(
    entities = [UserData::class, MessageData::class],
    version = 8,
    exportSchema = true
)
abstract class MyRoomDatabase : RoomDatabase() {
    abstract fun myUserDataDao(): UserDataDAO?
    abstract fun myMessageDataDao(): MessageDataDAO?


    companion object {
        // marking the instance as volatile to ensure atomic access to the variable
        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        fun getDatabase(context: Context): MyRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabase::class.java,
                    "sync_database"
                )
                    .addMigrations()
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    .fallbackToDestructiveMigration()
                    .addCallback(roomDatabaseCallback)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        /**
         * Override the onOpen method to populate the database.
         * For this sample, we clear the database every time it is created or opened.
         * If you want to populate the database only when the database is created for the 1st time,
         * override MyRoomDatabase.Callback()#onCreate
         */
        private val roomDatabaseCallback: Callback =
            object : Callback() {
            }
    }
}