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
import android.os.Looper
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
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.andcreator.iconpack.R

import org.andcreator.iconpack.adapter.RequestsAdapter
import org.andcreator.iconpack.bean.RequestsBean
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.android.synthetic.main.fragment_request.loading
import org.andcreator.iconpack.bean.AdaptionBean
import org.andcreator.iconpack.util.*
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
    private lateinit var appsList: ArrayList<RequestsBean>

    /**
     * 选中列表
     */
    private var checked: ArrayList<Boolean> = ArrayList()

    /**
     * 列表适配器
     */
    private var adapter: RequestsAdapter? = null

    /**
     * 反馈文本信息
     */
    private val message = StringBuilder()

    private val myFilesName = ArrayList<String>()

    private val myFiles = ArrayList<File>()

    private lateinit var fileZip: File

    private lateinit var thread: Thread

    private var mHandler= @SuppressLint("HandlerLeak")
    object: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1 ->{
                    zipLoad.progress = msg.arg1
                }
                2 ->{
                    zipLoad.progress = msg.arg1
                }
                3 ->{
                    sendEmail(fileZip)
                    Toast.makeText(context, "选择邮箱会自动填充作者邮箱地址", Toast.LENGTH_SHORT).show()
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
        view.setPadding(0, Utils.getStatusBarHeight(requireContext()), 0, 0)
        initView()
    }

    private fun initView(){
        recyclerApps.layoutManager = LinearLayoutManager(requireContext())

        AppAdaptationHelper.setContext(requireActivity()).getResolveInfo {
            appsList = it
            if (isDestroyed){
                if (loading.visibility == View.VISIBLE){
                    loading.visibility = View.GONE
                }
                notAdaptation.text = getString(R.string.not_adapter) + appsList.size
            }
            checked.clear()
            for (value in appsList) {
                checked.add(false)
            }
            adapter = RequestsAdapter(requireContext(), appsList, checked)
            recyclerApps.adapter = adapter
            recyclerApps.addOnScrollListener(object : HideScrollListener() {})

            adapter?.setOnSelectListener(object : RequestsAdapter.OnSelectListener {
                override fun onSelected(size: Int) {
                    sendRequest.text = "已选择 $size"
                }
            })

        }.getAdaptionCountForRequest {
            adaptation.text = getString(R.string.adapter) + it

        }

        selectAll.setOnClickListener {
            adapter?.selectAll()
        }

        sendRequest.setOnClickListener {
            send()
        }
    }

    private fun send() {
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
                        for ((index,value) in adapter!!.getSelect().withIndex()){
                            if (value){
                                saveIcon(appsList[index].icon!!, appsList[index].name?.toLowerCase()?.replace(" ", "_")!!)

                                val msg = Message()
                                msg.what = 1
                                msg.arg1 = (index * 50) / adapter!!.getSelect().size
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
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), "${requireActivity().packageName}.provider", path))
        startActivity(Intent.createChooser(intent, "请选择邮件类应用"))
    }

    interface Callbacks {
        fun callback(position: Int)
    }

    private lateinit var callbacks: Callbacks

    fun setCallbackListener(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    private fun onHide() {
        sendRequest.animate()
            .setDuration(200)
            .translationY(DisplayUtil.dip2px(context, 54f).toFloat())
            .start()
        callbacks.callback(0)
    }

    private fun onShow() {
        sendRequest.animate()
            .setDuration(200)
            .translationY(0F)
            .start()
        callbacks.callback(1)
    }

    private fun getMessage(): String{

        message.clear()

        message.append("Android version: Android ${android.os.Build.VERSION.RELEASE}\r\n")
        message.append("Device: ${android.os.Build.MODEL}\r\n")
        message.append("Manufacturer: ${android.os.Build.BRAND}\r\n")
        message.append("DPI: ${requireContext().displayMetrics.densityDpi}dpi\r\n")
        message.append("Resolution: ${requireContext().displayMetrics.widthPixels}x${requireContext().displayMetrics.heightPixels}\r\n")
        message.append("Device Language: ${Locale.getDefault().language}\r\n")
        message.append("\r\n")
        message.append("\r\n")

        var boolean = false
        if (adapter != null) {

            for ((index, value) in adapter!!.getSelect().withIndex()){
                if (value){
                    boolean = true
                    message.append("<!-- ${appsList[index].name} -->\r\n")
                    message.append("<item component=\"ComponentInfo{${appsList[index].pagName}/${appsList[index].activityName}}\" drawable=\"${appsList[index].name?.toLowerCase()?.replace(" ", "_")}\" />")
                    message.append("\r\n")
                }
            }

        }
        if (!boolean){
            return ""
        }

        message.append("\r\n")
        message.append("\r\n")

        message.append("App Version: ${Utils.getAppVersionName(requireContext())}")

        return message.toString()
    }

    private fun saveIcon(icon: Drawable, name: String) {

        val fileName = containsName(name)
        myFilesName.add(fileName)

        val bmp = getBitmapFromDrawable(icon)

        val file = File(requireActivity().externalCacheDir, "$fileName.png")
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
