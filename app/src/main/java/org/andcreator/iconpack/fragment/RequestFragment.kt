package org.andcreator.iconpack.fragment


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_icons.*
import org.andcreator.iconpack.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import org.andcreator.iconpack.adapter.RequestsAdapter
import org.andcreator.iconpack.bean.RequestsBean
import kotlinx.android.synthetic.main.fragment_request.*
import org.andcreator.iconpack.util.DBHelper
import org.jetbrains.anko.custom.async
import kotlinx.android.synthetic.main.fragment_request.loading
import org.andcreator.iconpack.bean.AdaptionBean
import org.andcreator.iconpack.util.Utils
import org.andcreator.iconpack.util.doAsyncTask
import org.andcreator.iconpack.util.onUI
import org.jetbrains.anko.displayMetrics
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.CRC32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 *
 */
class RequestFragment : BaseFragment() {

    /**
     * 未适配列表
     */
    private var appsList: ArrayList<RequestsBean> = ArrayList()
    /**
     * 选中列表
     */
    private var checked: ArrayList<Boolean> = ArrayList()
    /**
     * 已适配列表
     */
    private var adaptations: ArrayList<AdaptionBean> = ArrayList()
    private lateinit var adapter: RequestsAdapter
    private val message = StringBuilder()

    private val myFilesName = ArrayList<String>()
    private val myFiles = ArrayList<File>()

    private lateinit var fileZip: File

    private var waysAdaptions = 0

    private lateinit var thread: Thread

    private var mHandler= @SuppressLint("HandlerLeak")
    object: Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                1 ->{
                    zipLoad.progress = msg.arg1
                }
                2 ->{
                    zipLoad.progress = msg.arg1
                }
                3 ->{
                    sendEmail(fileZip)

                    zipLoad.visibility = View.GONE

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        recyclerApps.layoutManager = LinearLayoutManager(context!!)
        adapter = RequestsAdapter(context!!, appsList, checked)

        recyclerApps.adapter = adapter

        recyclerApps.addOnScrollListener(object : HideScrollListener() {

        })

        doAsyncTask {
            parser()
            loadData()
            if (isDestroyed){
                onUI {
                    if (loading.visibility == View.VISIBLE){
                        loading.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    fun send() {
        val s = getMessage()

        if (s.isNotEmpty()){

            if (zipLoad.visibility != View.VISIBLE){

                zipLoad.progress = 0
                zipLoad.visibility = View.VISIBLE

                thread = object : Thread(){
                    override fun run() {
                        super.run()

                        myFiles.clear()
                        myFilesName.clear()

                        val file = File(activity!!.externalCacheDir,"requests-${SimpleDateFormat("yyyy-MM-dd").format(Date())}.txt")

                        var out: FileOutputStream? = null
                        try {
                            if (!file.exists()) {
                                val files = File(file.parent)
                                files.mkdirs()
                                file.createNewFile()
                            }

                            out = FileOutputStream(file,false)
                            out.write(s.toByteArray())

                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            try {
                                out?.flush()
                                out?.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }

                        myFiles.add(file)
                        for ((index,value) in adapter.getSelect().withIndex()){
                            if (value){
                                saveIcon(appsList[index+1].icon!!, appsList[index+1].pagName?.toLowerCase()?.replace(".", "_")!!)

                                val msg = Message()
                                msg.what = 1
                                msg.arg1 = (index * 50) / adapter.getSelect().size
                                mHandler.sendMessage(msg)
                            }
                        }

                        //要压缩的文件的路径
                        fileZip = File(activity!!.externalCacheDir, "Requests-${SimpleDateFormat("yyyy-MM-dd").format(Date())}.zip")
                        try {
                            if (!fileZip.exists()) {
                                val fileZips = File(fileZip.parent)
                                fileZips.mkdirs()
                            }

                            val zipOutputStream = ZipOutputStream(CheckedOutputStream(FileOutputStream(fileZip), CRC32()))

                            for((index,value) in myFiles.withIndex()) {

                                val msg = Message()
                                msg.what = 2
                                msg.arg1 = 50 + ((index * 50) / myFiles.size)
                                mHandler.sendMessage(msg)

                                Log.e("fileLength", "${value.length()}")
                                zipOutputStream.putNextEntry(ZipEntry(value.name))
                                val bis  = BufferedInputStream(FileInputStream(value))
                                var count = 0
                                val byteData = ByteArray(1024)
                                while ({ count = bis.read(byteData, 0, 1024);count != -1 }()) {
                                    zipOutputStream.write(byteData, 0, count)
                                }
                                bis.close()
                            }

                            zipOutputStream.flush()
                            zipOutputStream.close()

                            val msg = Message()
                            msg.what = 3
                            mHandler.sendMessage(msg)

                        }catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                thread.start()

            }
        }else {
            callbacks.callback(2)
        }

    }

    private fun sendEmail(path: File){

        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
// intent.putExtra(Intent.EXTRA_EMAIL, email)，结果将匹配不到任何应用
        val uri = Uri.parse("mailto:"+resources.getString(R.string.mail))
        val email = arrayOf(resources.getString(R.string.mail))
        val intent = Intent(Intent.ACTION_SEND, uri)
        intent.type = "application/octet-stream"
        intent.putExtra(Intent.EXTRA_EMAIL, email)
        intent.putExtra(Intent.EXTRA_SUBJECT, "致开发者") // 主题
        intent.putExtra(Intent.EXTRA_TEXT, "") // 正文
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context!!, "${activity!!.packageName}.provider", path))
        startActivity(Intent.createChooser(intent, "请选择邮件类应用"))
        applied()
    }

    private fun loadData(){
        appsList.clear()
        message.clear()
        waysAdaptions = 0
        val redPkg = DBHelper.getInstance(context!!).getRequested().map(RequestsBean::pagName)
        val pm = context!!.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN,null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(mainIntent,0)
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
        for (reInfo: ResolveInfo in resolveInfos){
            var isHave = false
            val pkgName = reInfo.activityInfo.packageName // 获得应用程序的包名
            for (adaptionBean: AdaptionBean in adaptations){
                if (adaptionBean.pagName == pkgName && reInfo.activityInfo.name == adaptionBean.activityName){
                    waysAdaptions++
                    isHave = true
                    //包名和activity名一样才算适配
                    break
                }
            }
            if (isHave){
                continue
            }

            val activityName = reInfo.activityInfo.name // 获得该应用程序的启动Activity的name
            val appLabel = reInfo.loadLabel(pm) as String // 获得应用程序的Label
            val icon = reInfo.loadIcon(pm) // 获得应用程序图标
            // 为应用程序的启动Activity 准备Intent
            val launchIntent = Intent()
            launchIntent.component = ComponentName(
                pkgName,
                activityName
            )
            // 创建一个AppInfo对象，并赋值
            if (!redPkg.contains(pkgName)) {
                appsList.add(RequestsBean(0,icon,appLabel,pkgName,activityName,appsList.size,waysAdaptions, 0)) // 添加至列表中
            } else {
                appsList.add(RequestsBean(0,icon,appLabel,pkgName,activityName,appsList.size,waysAdaptions, 2)) // 添加至列表中
            }

            checked.add(false)
        }

        appsList.add(0, RequestsBean(0,null," ",null,null,appsList.size,waysAdaptions,1))
        Log.e("不可能是：",waysAdaptions.toString())

    }

    private fun parser(){
        val xml = context!!.resources.getXml(R.xml.appfilter)
        var type = xml.eventType
        try {
            while (type != XmlPullParser.END_DOCUMENT){
                when(type){
                    XmlPullParser.START_TAG ->{
                        if (xml.name == "item"){
                            val pkgActivity = xml.getAttributeValue(0)
                            if (pkgActivity.indexOf("{")+1 < pkgActivity.indexOf("/") && pkgActivity.indexOf("/")+1 < pkgActivity.indexOf("}")){
                                adaptations.add(AdaptionBean(pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/")), pkgActivity.substring(pkgActivity.indexOf("/")+1,pkgActivity.indexOf("}")),""))
                            }
                        }
                    }
                    XmlPullParser.TEXT ->{

                    }
                }
                type = xml.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun applied() {
        doAsync {
            uiThread {
                adapter.deSelectAll()
            }
        }
    }

    interface Callbacks {
        fun callback(position: Int)
    }

    private lateinit var callbacks: Callbacks

    fun setCallbackListener(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    private fun onHide() {
        callbacks.callback(0)
    }

    private fun onShow() {
        callbacks.callback(1)
    }

    private fun getMessage(): String{

        message.clear()

        message.append("Android version: Android ${android.os.Build.VERSION.RELEASE}\r\n")
        message.append("Device: ${android.os.Build.MODEL}\r\n")
        message.append("Manufacturer: ${android.os.Build.BRAND}\r\n")
        message.append("DPI: ${context!!.displayMetrics.densityDpi}dpi\r\n")
        message.append("Resolution: ${context!!.displayMetrics.widthPixels}x${context!!.displayMetrics.heightPixels}\r\n")
        message.append("Device Language: ${Locale.getDefault().language}\r\n")
        message.append("\r\n")
        message.append("\r\n")

        var boolean = false
        for ((index,value) in adapter.getSelect().withIndex()){
            if (value){
                boolean = true
                message.append("<!-- ${appsList[index+1].name} -->\r\n")
                message.append("<item component=\"ComponentInfo{${appsList[index+1].pagName}/${appsList[index+1].activityName}}\" drawable=\"${appsList[index+1].pagName?.toLowerCase()?.replace(".", "_")}\" />")
                message.append("\r\n")
                doAsync {
                    val b = appsList[index+1]
                    b.type = 2
                    DBHelper.getInstance(context!!).insertOrUpdate(b)
                }
            }
        }

        if (!boolean){
            return ""
        }

        message.append("\r\n")
        message.append("\r\n")

        message.append("App Version: ${Utils.getAppVersionName(context!!)}")

        return message.toString()
    }

    private fun saveIcon(icon: Drawable, name: String) {

        val fileName = containsName(name)
        myFilesName.add(fileName)

        val bmp = getBitmapFromDrawable(icon)

        val file = File(activity!!.externalCacheDir, "$fileName.png")
        val out = FileOutputStream(file)
        try {
            if (!file.exists()) {
                val files = File(file.parent)
                files.mkdirs()
                file.createNewFile()
            }

            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            out.flush()
            out.close()
        }

        myFiles.add(file)
    }

    private fun containsName(name: String) :String {

        return if (myFilesName.contains(name)){
            containsName("$name-")
        }else {
            name
        }
    }


    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    //滑动监听
    internal open inner class HideScrollListener : RecyclerView.OnScrollListener() {
        private val HIDE_HEIGHT = 40
        private var scrolledInstance = 0
        private var toolbarVisible = true

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (toolbarVisible && dy > 0 || !toolbarVisible && dy < 0) {
                //recycler向上滚动时dy为正，向下滚动时dy为负数
                scrolledInstance += dy
            }
            if (scrolledInstance > HIDE_HEIGHT && toolbarVisible) {//当recycler向上滑动距离超过设置的默认值并且toolbar可见时，隐藏toolbar和fab
                onHide()
                scrolledInstance = 0
                toolbarVisible = false
            } else if (scrolledInstance < -HIDE_HEIGHT && !toolbarVisible) {//当recycler向下滑动距离超过设置的默认值并且toolbar不可见时，显示toolbar和fab
                onShow()
                scrolledInstance = 0
                toolbarVisible = true
            }
        }
    }

}
