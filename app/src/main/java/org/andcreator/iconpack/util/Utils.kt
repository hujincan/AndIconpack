package org.andcreator.iconpack.util

import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.system.Os
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import org.andcreator.iconpack.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object Utils {

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val installed: Boolean
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Throwable) {
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
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        }
    }

    /**
     * 获取App版本号名称
     */
    fun getAppVersionName(context: Context): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName

    }

    /**
     * 设置debug模式开闭
     * @param status 开关状态
     * @param context 上下文
     */
    fun setDebug(status: Boolean, context: Context) {
        val sharedPreferences = context.getSharedPreferences("debug", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("debug", status)
        editor.apply()
    }

    fun isShowDebug(context: Context): Boolean {
        return context.getSharedPreferences("debug", Context.MODE_PRIVATE).getBoolean("debug", false)
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

    @Throws(IOException::class)
    fun saveBitmap(
        @NonNull context: Context, @NonNull bitmap: Bitmap,
        @NonNull format: Bitmap.CompressFormat, @NonNull mimeType: String,
        @NonNull displayName: String
    ) {
        val relativeLocation = Environment.DIRECTORY_PICTURES  + "/Smalite"

        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)

            var stream: OutputStream? = null
            var uri: Uri? = null

            try {
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                uri = resolver.insert(contentUri, contentValues)

                if (uri == null) {
                    throw IOException("Failed to create new MediaStore record.")
                }

                stream = resolver.openOutputStream(uri)

                if (stream == null) {
                    throw IOException("Failed to get output stream.")
                }

                if (!bitmap.compress(format, 95, stream)) {
                    throw IOException("Failed to save bitmap.")
                }
            } catch (e: IOException) {
                if (uri != null) {
                    resolver.delete(uri, null, null)
                }

                throw IOException(e)
            } finally {
                stream?.close()
            }
        } else {
            val filePath = OtherUtil.getSDLogPath() + "/" + relativeLocation
            val dir = File(filePath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val img = File(dir, "$displayName.png")
            try {
                val fos = FileOutputStream(img)
                bitmap.compress(format, 95, fos)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                Log.e("saveBitmap", e.message!!)
            } finally {
                if (!bitmap.isRecycled) System.gc()
                MediaScannerConnection.scanFile(context, arrayOf(img.absolutePath), arrayOf(mimeType), null)
            }
        }
    }

    fun isShowAgreement(context: Context): Boolean {
        return context.getSharedPreferences("saveIcon", Context.MODE_PRIVATE).getBoolean("saveIcon", false)
    }

    fun setAgreementStatus(status: Boolean, context: Context) {
        val sharedPreferences = context.getSharedPreferences("saveIcon", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("saveIcon", status)
        editor.apply()
    }
}