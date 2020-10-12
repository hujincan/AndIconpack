package org.andcreator.iconpack.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toolbar
import org.andcreator.iconpack.R

object Utils {

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val installed: Boolean
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        return installed
    }

    fun getIconResId(r: Resources, p: String, name: String): Int {
        val res = r.getIdentifier(name, "drawable", p)
        return if (res != 0) {
            res
        } else {
            0
        }
    }

    /**
     * 获取App版本号
     */
    fun getAppVersion(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        }
    }

    /**
     * 获取App版本号名称
     */
    fun getAppVersionName(context: Context): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName

    }

    /**
     * 实现文本复制功能
     * @param content
     */
    fun copy(content: String, context: Context) {
        // 得到剪贴板管理器
        val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = ClipData.newPlainText(context.getString(R.string.app_name), content.trim())
        cmb.setPrimaryClip(data)
    }

    val Any.objId: String
        get() {
            return System.identityHashCode(this).toString(16)
        }

    /**
     * 获取界面到屏幕顶部的高度
     */
    private fun getAppTopHeight(context: Activity): Int{
        val frame = Rect()
        context.window.decorView.getWindowVisibleDisplayFrame(frame)
        Log.e("appTopHeight",frame.top.toString())
        return frame.top
    }

    /**
     * Toolbar设置paddingTop
     */
    private fun fixToolbarPadding(paddingTop: Int, toolbar: Toolbar) {
        if (paddingTop in 1..300){
            Log.e("状态栏",paddingTop.toString())
            toolbar.setPadding(0, paddingTop, 0, 0)
            val lp = toolbar.layoutParams
            lp.height = paddingTop + toolbar.height
        }
//        if (isStatusBarVisible()){
//            val paddingTop = getStatusBarHeight()
//            Log.e("状态栏",getStatusBarHeight().toString())
//            toolbar.setPadding(0, paddingTop, 0, 0)
//            val lp = toolbar.layoutParams
//            lp.height = paddingTop + toolbar.height
//        }
    }

    /**
     * 获取状态栏的高度
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return try {
            context.resources.getDimensionPixelSize(resourceId)
        } catch (e: Resources.NotFoundException) {
            0
        }

    }

    fun isOnMainThread(): Boolean {
        return Thread.currentThread() === Looper.getMainLooper().thread
    }
}