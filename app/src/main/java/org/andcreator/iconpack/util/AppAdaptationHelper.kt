package org.andcreator.iconpack.util


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import org.andcreator.iconpack.R
import org.andcreator.iconpack.bean.AdaptionBean
import org.andcreator.iconpack.bean.IconsBean
import org.andcreator.iconpack.bean.RequestsBean
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * @author andrew
 * @date  2020/4/5 13:45
 */
object AppAdaptationHelper {

    /**
     * 总的适配数量
     */
    private var adaptations = 0

    /**
     * 总的适配数据
     */
    private val allAdaptions = ArrayList<AdaptionBean>()

    /**
     * 总的适配单个图标数据
     */
    private val allAdaptionsIcon = ArrayList<AdaptionBean>()

    /**
     * 新的已适配数据
     */
    private val newAdaptions = ArrayList<AdaptionBean>()

    /**
     * 新的适配数据
     */
    private var adaptationsNew: ArrayList<AdaptionBean> = ArrayList()

    /**
     * 旧的适配数据
     */
    private var adaptationsOld: ArrayList<AdaptionBean> = ArrayList()

    /**
     * 总的适配图标数据
     */
    private val icons = ArrayList<Int>()

    /**
     * 图标列表
     */
    private val iconsList = ArrayList<IconsBean>()

    /**
     * 未适配列表
     */
    private var appsList: ArrayList<RequestsBean> = ArrayList()

    /**
     * 随机四个Icon在icons列表的位置
     */
    private var randomNumber = Array(4) {0}

    /**
     * 随机四个Icon在icons列表的位置
     */
    private val randomIcon = java.util.ArrayList<Int>()

    private val err = StringBuilder()
    private val sb = StringBuilder()

    private var parserOld = ""

    private var context: Context? = null

    private var loadAppFilter: ((String) -> Unit)? = null
    private var loadAppFilterError: ((String) -> Unit)? = null
    private var loadIconCount: ((Int) -> Unit)? = null
    private var loadAppCount: ((Int) -> Unit)? = null
    private var loadAdaptionCount: ((Int) -> Unit)? = null
    private var loadAdaptionCountForRequest: ((Int) -> Unit)? = null
    private var loadUpdateIcon: ((ArrayList<AdaptionBean>) -> Unit)? = null
    private var loadUpdateAdaptionIcon: ((ArrayList<AdaptionBean>) -> Unit)? = null
    private var loadRandomIcon: ((ArrayList<Int>) -> Unit)? = null
    private var loadAdaptationIcon: ((ArrayList<IconsBean>) -> Unit)? = null
    private var loadResolveInfo: ((ArrayList<RequestsBean>) ->Unit)? = null

    private var adaptions = 0

    private var appCount = 0

    fun getAppFilterListener(loadAppFilter: (String) -> Unit): AppAdaptationHelper {
        if (sb.isNotEmpty()) {
            onUI {
                loadAppFilter.invoke(sb.toString())
            }
        } else {
            Log.e("newAdaption", "loadAppFilter赋值了")
        }
        this.loadAppFilter = loadAppFilter
        return this
    }

    fun getAppFilterError(loadAppFilterError: (String) -> Unit): AppAdaptationHelper {
        if (err.isNotEmpty()) {
            onUI {
                loadAppFilterError.invoke(err.toString())
            }
        } else {
        }
        this.loadAppFilterError = loadAppFilterError
        return this
    }

    fun getUpdateIcon(loadUpdateIcon: (ArrayList<AdaptionBean>) -> Unit): AppAdaptationHelper {
        if (adaptationsNew.isNotEmpty() && parserOld.isNotEmpty()) {
            onUI {
                loadUpdateIcon.invoke(adaptationsNew)
            }
        } else if (allAdaptionsIcon.isNotEmpty() && parserOld.isEmpty()) {
            onUI {
                loadUpdateIcon.invoke(allAdaptionsIcon)
            }
        } else {
            Log.e("newAdaption", "loadUpdateIcon赋值了")
        }
        this.loadUpdateIcon = loadUpdateIcon
        return this
    }

    fun getUpdateAdaptionIcon(loadUpdateAdaptionIcon: (ArrayList<AdaptionBean>) -> Unit): AppAdaptationHelper {
        Log.e("newAdaption", "结果如何？")
        if (newAdaptions.isNotEmpty()) {
            Log.e("newAdaption", "？${newAdaptions.size}")
            onUI {
                loadUpdateAdaptionIcon.invoke(newAdaptions)
            }
        } else {
            Log.e("newAdaption", "loadUpdateAdaptionIcon赋值了")
        }
        this.loadUpdateAdaptionIcon = loadUpdateAdaptionIcon
        return this
    }

    fun getRandomIcon(loadRandomIcon: (ArrayList<Int>) -> Unit): AppAdaptationHelper {
        if (icons.isNotEmpty() && randomIcon.isNotEmpty()) {
            onUI {
                randomIcon.clear()
                var index = 0
                while (index < 4) {
                    val r = Random.nextInt(icons.size)%(icons.size + 1)

                    var bo = true
                    for (j in randomNumber) {
                        if (j == r) {

                            bo = false
                            break
                        }
                    }
                    if (bo && index < 4) {
                        randomNumber[index] = r
                        index++
                        randomIcon.add(icons[r])
                    } else if (randomIcon.size >= icons.size - 1) {
                        index++
                        randomIcon.add(icons[r])
                    }
                }

                loadRandomIcon.invoke(randomIcon)
            }
        } else {
        }
        this.loadRandomIcon = loadRandomIcon
        return this
    }

    fun getIconCount(loadIconCount: (Int) -> Unit): AppAdaptationHelper {
        if (adaptations != 0) {
            onUI {
                loadIconCount.invoke(adaptations)
            }
        } else {
            Log.e("newAdaption", "loadIconCount赋值了")
        }
        this.loadIconCount = loadIconCount
        return this
    }

    fun getAdaptationIcon(loadAdaptationIcon: (ArrayList<IconsBean>) -> Unit): AppAdaptationHelper {
        if (iconsList.isNotEmpty()) {
            onUI {
                Log.e("IconsFragment", "准备回调")
                loadAdaptationIcon.invoke(iconsList)
            }
        }
        this.loadAdaptationIcon = loadAdaptationIcon
        return this
    }

    fun getAppCount(loadAppCount: (Int) -> Unit): AppAdaptationHelper {
        if (allAdaptions.isNotEmpty() && appCount > 0) {
            onUI {
                loadAppCount.invoke(appCount)
            }
        } else {
        }
        this.loadAppCount = loadAppCount
        return this
    }

    fun getResolveInfo(loadResolveInfo: (ArrayList<RequestsBean>) -> Unit): AppAdaptationHelper  {
        if (appsList.isNotEmpty()) {
            onUI {
                loadResolveInfo.invoke(appsList)
                logger("不空")
            }
        } else {
            logger("空")
        }
        this.loadResolveInfo = loadResolveInfo
        return this
    }

    fun getAdaptionCount(loadAdaptionCount: (Int) -> Unit): AppAdaptationHelper {
        if (adaptions != 0 && appsList.isNotEmpty()) {
            onUI {
                loadAdaptionCount.invoke(adaptions)
            }
        } else {
            Log.e("newAdaption", "loadIconCount赋值了")
        }
        this.loadAdaptionCount = loadAdaptionCount
        return this
    }

    fun getAdaptionCountForRequest(loadAdaptionCountForRequest: (Int) -> Unit): AppAdaptationHelper {
        if (adaptions != 0 && appsList.isNotEmpty()) {
            onUI {
                loadAdaptionCountForRequest.invoke(adaptions)
            }
        } else {
        }
        this.loadAdaptionCountForRequest = loadAdaptionCountForRequest
        return this
    }

    fun setContext(context: Context): AppAdaptationHelper {
        if (this.context == null) {
            this.context = context
            if (icons.size == 0) {
                //加载所有图标（随机四个图标）
                loadIcons()
                parserDrawable()
            }
        }
        return this
    }

    /**
     * 显示更新了哪些图标
     */
    private fun showUpdateIcons() {

        if (File(context!!.filesDir, "appfilter.xml").exists()){
            //获取旧版数据
            parserOld = parserOld()
            if (parserOld.isNotEmpty()){
                for (v in allAdaptions) {
                    if (checkIndexOf(adaptationsOld, v) < 0){
                        adaptationsNew.add(v)
                    }
                }
                onUI {
                    loadUpdateIcon?.invoke(adaptationsNew)
                }
                //相对于旧版更新了哪些图标
                showAdaptions(adaptationsNew)
            }else{
                onUI {
                    loadUpdateIcon?.invoke(allAdaptionsIcon)
                }
                //第一次安装直接显示所有图标
                showAdaptions(allAdaptions)
            }
        } else {
            onUI {
                loadUpdateIcon?.invoke(allAdaptionsIcon)
            }
            //第一次安装直接显示所有图标
            showAdaptions(allAdaptions)
        }
    }

    /**
     * 新适配设备上哪些图标
     * @param iconsList 新版本更新的图标数据
     */
    private fun showAdaptions(iconsList: ArrayList<AdaptionBean>){
        newAdaptions.clear()
        val pm = context!!.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN,null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(mainIntent,0)
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
        for (reInfo: ResolveInfo in resolveInfos){
            val pkgName = reInfo.activityInfo.packageName // 获得应用程序的包名
            for (adaptionBean: AdaptionBean in iconsList){
                if (adaptionBean.pagName == pkgName && reInfo.activityInfo.name == adaptionBean.activityName){
                    newAdaptions.add(adaptionBean)
                    //包名和activity名一样才算适配
                    break
                }
            }
        }
        appCount = resolveInfos.size
        Log.e("APPCount", "$appCount")
        onUI {
            loadAppCount?.invoke(resolveInfos.size)
            loadUpdateAdaptionIcon?.invoke(newAdaptions)
        }
    }

    /**
     * 检查当前版本对于旧版的适配数量是否有增加
     */
    private fun checkIndexOf(list: ArrayList<AdaptionBean>, item: AdaptionBean): Int{
        for (index in list.indices){
            if (list[index].pagName == item.pagName || list[index].icon == item.icon ){
                return index
            }
        }
        return -1
    }

    /**
     * 获取旧版appfilter
     */
    private fun parserOld(): String{

        val appFilter = File(context!!.filesDir, "appfilter.xml")
        val content = StringBuilder()
        appFilter.forEachLine { line ->
            content.append(line)
            content.append("\r\n")
        }

        if (content.isNotEmpty()){

            val xml = XmlPullParserFactory.newInstance().newPullParser()
            xml.setInput(StringReader(content.toString()))
            var type = xml.eventType
            try {
                while (type != XmlPullParser.END_DOCUMENT){
                    when(type){
                        XmlPullParser.START_TAG ->{
                            if (xml.name == "item"){
                                val pkgActivity = xml.getAttributeValue(0)
                                if (pkgActivity.indexOf("{")+1 < pkgActivity.indexOf("/") && pkgActivity.indexOf("/")+1 < pkgActivity.indexOf("}")){
                                    adaptationsOld.add(AdaptionBean("", pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/")), pkgActivity.substring(pkgActivity.indexOf("/")+1,pkgActivity.indexOf("}")),xml.getAttributeValue(0)))
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
        return content.toString()
    }

    private fun loadIcons(){
        iconsList.clear()
        val xml = context!!.resources.getXml(R.xml.drawable)
        var type = xml.eventType
        var category = "All"
        doAsyncTask {
            if (icons.size == 0){
                try {
                    while (type != XmlPullParser.END_DOCUMENT){
                        when(type){
                            XmlPullParser.START_TAG ->{
                                if (xml.name == "category" && xml.attributeCount == 1){
                                    category = xml.getAttributeValue(0)
                                }
                                if (xml.name == "item"){
                                    val drawableString = xml.getAttributeValue(0)
                                    val drawableId = context!!.resources.getIdentifier(drawableString,"drawable",this.context!!.packageName)
                                    icons.add(drawableId)

                                    if (xml.attributeCount == 1){
                                        iconsList.add(IconsBean(category, drawableId, drawableString, drawableString))
                                    }else{
                                        val drawableName = xml.getAttributeValue(1)
                                        iconsList.add(IconsBean(category, drawableId, drawableName, drawableString))
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
                onUI {
                    loadAdaptationIcon?.invoke(iconsList)
                }
            }

            randomIcon.clear()
            var index = 0
            while (index < 4) {
                val r = Random.nextInt(icons.size)%(icons.size + 1)

                var bo = true
                for (j in randomNumber) {
                    if (j == r) {

                        bo = false
                        break
                    }
                }
                if (bo && index < 4) {
                    randomNumber[index] = r
                    index++
                    randomIcon.add(icons[r])
                } else if (randomIcon.size >= icons.size - 1) {
                    index++
                    randomIcon.add(icons[r])
                }
            }
            onUI {
                loadRandomIcon?.invoke(randomIcon)
            }

            parser()
        }
    }

    /**
     * 获取当前版本appfilter总数据
     */
    private fun parser(){

        doAsyncTask {

            allAdaptions.clear()
            val xml = context!!.resources.getXml(R.xml.appfilter)
            var type = xml.eventType
            var isFind = false
            try {
                while (type != XmlPullParser.END_DOCUMENT){
                    when(type){
                        XmlPullParser.START_TAG ->{
                            if (xml.name == "item"){
                                val pkgActivity = xml.getAttributeValue(0)
                                val drawable = xml.getAttributeValue(1)
                                if (pkgActivity.indexOf("{") > 0 && pkgActivity.indexOf("{")+1 < pkgActivity.indexOf("/") && pkgActivity.indexOf("/")+1 < pkgActivity.indexOf("}")){
                                    isFind = false
                                    for (drawableInfo in iconsList) {
                                        if (drawableInfo.drawableName == drawable) {
                                            allAdaptions.add(AdaptionBean(drawableInfo.name, pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/")), pkgActivity.substring(pkgActivity.indexOf("/")+1,pkgActivity.indexOf("}")), drawable))
                                            isFind = true
                                            break
                                        }
                                    }

                                    if (!isFind) {
                                        allAdaptions.add(AdaptionBean(drawable, pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/")), pkgActivity.substring(pkgActivity.indexOf("/")+1,pkgActivity.indexOf("}")), drawable))
                                    }
                                    sb.append("<item component=\"$pkgActivity\" drawable=\"${drawable}\" />\r\n")
                                }else{
                                    err.append("在${pkgActivity} 附近处有一处错误\r\n")
                                }
                            }
                        }
                        XmlPullParser.TEXT ->{

                        }
                    }
                    type = xml.next()
                }
                if (err.isNotEmpty()) {
                    onUI {
                        loadAppFilterError?.invoke(err.toString())
                    }
                }
                if (sb.isNotEmpty()) {
                    onUI {
                        loadAppFilter?.invoke(sb.toString())
                    }
                }
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // 获取适配了设备上APP情况
            resolveInfo()
        }
    }

    private fun parserDrawable(){

        doAsyncTask {
            adaptations = 0
            allAdaptionsIcon.clear()
            val xml = context!!.resources.getXml(R.xml.drawable)
            var type = xml.eventType
            try {
                while (type != XmlPullParser.END_DOCUMENT) {
                    when (type) {
                        XmlPullParser.START_TAG -> {
                            if (xml.name == "item"){
                                val drawableString = xml.getAttributeValue(0)

                                if (xml.attributeCount == 1){
                                    allAdaptionsIcon.add(AdaptionBean(drawableString, "", "", drawableString))
                                }else{
                                    val drawableName = xml.getAttributeValue(1)
                                    allAdaptionsIcon.add(AdaptionBean(drawableName, "", "", drawableString))
                                }
                                adaptations++
                            }
                        }
                        XmlPullParser.TEXT -> {

                        }
                    }
                    type = xml.next()
                }
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            showUpdateIcons()
        }
    }

    /**
     * 获取适配设备情况
     */
    private fun resolveInfo() {
        doAsyncTask {

            appsList.clear()
            adaptions = 0
            val pm = context!!.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN,null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            // 调用系统排序 ， 根据name排序
            // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
            Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
            for (reInfo: ResolveInfo in resolveInfos){
                var isHave = false
                val pkgName = reInfo.activityInfo.packageName // 获得应用程序的包名
                logger("get $pkgName")
                for (adaptionBean: AdaptionBean in allAdaptions){
                    if (adaptionBean.pagName == pkgName && reInfo.activityInfo.name == adaptionBean.activityName){
                        adaptions++
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
                appsList.add(RequestsBean(icon,appLabel,pkgName,activityName)) // 添加至列表中
                logger("add $appLabel")
            }
            onUI {
                loadResolveInfo?.invoke(appsList)
                loadAdaptionCount?.invoke(adaptions)
                loadAdaptionCountForRequest?.invoke(adaptions)
            }
        }
    }
}