package org.andcreator.iconpack.fragment


import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.andcreator.iconpack.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import org.andcreator.iconpack.adapter.RequestsAdapter
import org.andcreator.iconpack.bean.RequestsBean
import kotlinx.android.synthetic.main.fragment_request.*
import org.andcreator.iconpack.adapter.DebugAdapter
import org.andcreator.iconpack.util.DBHelper
import org.jetbrains.anko.custom.async
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 *
 */
class DebugFragment : androidx.fragment.app.Fragment() {

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
    private var adaptations: ArrayList<String> = ArrayList()
    lateinit var adapter: DebugAdapter
    private val message = StringBuilder()

    private var waysAdaptions = 0

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
        recyclerApps.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context!!)
        adapter = DebugAdapter(context!!,appsList)

        recyclerApps.adapter = adapter

        doAsync{
            parser()
            loadData()
            uiThread{
                if (loading.visibility == View.VISIBLE){
                    loading.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()

            }
        }

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
            for (packageName: String in adaptations){
                if (packageName == pkgName){
                    waysAdaptions++
                    isHave = true
                    break
                }
            }
//            if (isHave){
//                continue
//            }

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
                            if (pkgActivity.indexOf("{")+1<pkgActivity.indexOf("/")){
                                val packageName = pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/"))

                                adaptations.add(packageName)
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

//    fun getMessage(): String{
//        message.clear()
//        for ((index,value) in adapter.getSelect().withIndex()){
//            if (value){
//
//                message.append("<!-- ${appsList[index+1].name} -->\r\n")
//                message.append("<item component=\"ComponentInfo{${appsList[index+1].pagName}/${appsList[index+1].activityName}}\" drawable=\"${appsList[index+1].name?.toLowerCase()?.replace(" ", "_")}\" />")
//                message.append("\r\n")
//                doAsync {
//                    val b = appsList[index+1]
//                    b.type = 2
//                    DBHelper.getInstance(context!!).insertOrUpdate(b)
//                }
//            }
//        }
//        return message.toString()
//    }

}
