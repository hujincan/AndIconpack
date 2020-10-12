package org.andcreator.iconpack.util

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.andcreator.iconpack.R


object SnackbarUtil {

    fun snackbarUtil(context: Context, views: View, content: String) {

        val snackbar = Snackbar.make(views, content, Snackbar.LENGTH_SHORT)
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.black_text))
        snackbar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundColor))
        snackbar.show()

    }
}