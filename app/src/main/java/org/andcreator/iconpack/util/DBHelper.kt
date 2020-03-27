package org.andcreator.iconpack.util

import android.content.Context
import androidx.room.Room
import org.andcreator.iconpack.bean.RequestsBean
import org.andcreator.iconpack.orm.AppDatabase

class DBHelper constructor(context: Context) {
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "appDB").build()

    companion object{
        @Volatile
        var INSTANCE: DBHelper? = null

        fun getInstance(context: Context): DBHelper {
            if (INSTANCE == null) {
                synchronized(DBHelper::class) {
                    if (INSTANCE == null) {
                        INSTANCE = DBHelper(context.applicationContext)
                    }
                }
            }
            return INSTANCE!!
        }
    }

    fun insertBean(beans: List<RequestsBean>) {
        for (bean: RequestsBean in beans) {
            db.appDao().insert(bean)
        }
    }

    fun insertBean(bean: RequestsBean) {
        db.appDao().insert(bean)
    }

    fun insertOrUpdate(bean: RequestsBean) {
        if (getById(bean.uid) != null) {
            update(bean)
        } else {
            insertBean(bean)
        }
    }

    fun getAll(): List<RequestsBean>? {
        return db.appDao().getAll()
    }

    fun getById(id: Int?): RequestsBean? {
        if (id == null) return null
        return db.appDao().getById(id)
    }

    fun getUnAdapted(): List<RequestsBean> {
        return db.appDao().getByType(0) ?:return emptyList()
    }

    fun getRequested(): List<RequestsBean> {
        return db.appDao().getByType(2) ?: return emptyList()
    }

    fun update(bean: RequestsBean) {
        db.appDao().update(bean)
    }

    fun detele(beans: List<RequestsBean>) {
        for (bean: RequestsBean in beans) {
            db.appDao().delete(bean)
        }
    }

    fun detele(bean: RequestsBean) {
        db.appDao().delete(bean)
    }
}