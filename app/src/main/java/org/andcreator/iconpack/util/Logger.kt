package org.andcreator.iconpack.util

import android.util.Log

/**
 * @author Andrew
 * @date 2020/08/04 20:02
 */

inline fun <reified T: Any> T.logger(value: String) {
    Log.d("Andrew", "${this.javaClass.simpleName} -> $value")
}