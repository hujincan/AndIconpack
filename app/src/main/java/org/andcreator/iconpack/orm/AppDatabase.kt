package org.andcreator.iconpack.orm

import androidx.room.Database
import androidx.room.RoomDatabase
import org.andcreator.iconpack.bean.RequestsBean

@Database(entities = [RequestsBean::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun appDao(): AppDao
}