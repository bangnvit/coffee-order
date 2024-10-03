package com.bangnv.cafeorder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bangnv.cafeorder.model.Food

@Database(entities = [Food::class], version = 2)
abstract class FoodDatabase : RoomDatabase() {

    abstract fun foodDAO(): FoodDAO

    companion object {
        private const val DATABASE_NAME = "food.db"
        private var instance: FoodDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): FoodDatabase {
            return instance ?: Room.databaseBuilder(
                context.applicationContext, FoodDatabase::class.java, DATABASE_NAME
            ).allowMainThreadQueries().build().also { instance = it }
//            Khi cần migration thêm version, dùng:
//            return instance ?: Room.databaseBuilder(context.applicationContext, FoodDatabase::class.java, DATABASE_NAME)
//                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_1_2)
//                .build().also { instance = it }
        }
//        private val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE food ADD COLUMN categoryId INTEGER NOT NULL DEFAULT 0")
//                database.execSQL("ALTER TABLE food ADD COLUMN categoryName TEXT")
//            }
//        }
    }
}