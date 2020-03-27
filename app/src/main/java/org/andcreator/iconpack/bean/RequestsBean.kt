package org.andcreator.iconpack.bean

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "requestsBean")
data class RequestsBean constructor(
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0,
    @Ignore
    var icon: Drawable? = null,
    var name: String? = null,
    var pagName: String? = null,
    var activityName: String? = null,
    var notAdaptation: Int = 0,
    var adaptation: Int = 0,
    var type: Int = 0) {
    constructor() :
            this(0)

}