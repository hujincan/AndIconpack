package org.andcreator.iconpack.orm

import androidx.room.*
import org.andcreator.iconpack.bean.RequestsBean

@Dao
interface AppDao {
    @Query("select * from requestsBean")
    fun getAll(): List<RequestsBean>?

    @Query("select * from requestsBean where type = (:type)")
    fun getByType(type: Int): List<RequestsBean>?

    @Query("select * from requestsBean where uid = (:id)")
    fun getById(id: Int): RequestsBean?

    @Insert
    fun insert(bean: RequestsBean)

    @Update
    fun update(bean: RequestsBean)

    @Delete
    fun delete(bean: RequestsBean)

}