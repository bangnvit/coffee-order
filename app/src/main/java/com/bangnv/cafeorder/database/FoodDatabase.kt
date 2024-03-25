package com.bangnv.cafeorder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bangnv.cafeorder.model.Food

@Database(entities = [Food::class], version = 1)
abstract class FoodDatabase : RoomDatabase() {

    abstract fun foodDAO(): FoodDAO?

    companion object {
        private const val DATABASE_NAME = "food.db"
        private var instance: FoodDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): FoodDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, FoodDatabase::class.java, DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build()
            }
            return instance
        }
    }
}