package org.andcreator.iconpack.fragment


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.andcreator.iconpack.R
import kotlinx.android.synthetic.main.fragment_home.*
import android.widget.Toast
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URISyntaxException
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.andcreator.iconpack.activity.ImageDialog
import org.andcreator.iconpack.activity.MainActivity
import org.andcreator.iconpack.adapter.HomeIconsAdapter
import org.andcreator.iconpack.bean.AdaptionBean
import org.andcreator.iconpack.util.doAsyncTask
import org.andcreator.iconpack.util.onUI
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : BaseFragment() {

    private var adaptations = 0
    private val allAdaptions = ArrayList<AdaptionBean>()
    private val newAdaptions = ArrayList<AdaptionBean>()

    companion object{
        private const val ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone"

        private const val INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{payCode}%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"
    }

    /**
     * 新的已适配列表
     */
    private var adaptationsNew: ArrayList<AdaptionBean> = ArrayList()
    /**
     * 已适配列表
     */
    private var adaptationsList: ArrayList<AdaptionBean> = ArrayList()
    /**
     * 旧的已适配列表
     */
    private var adaptationsOld: ArrayList<AdaptionBean> = ArrayList()

    private lateinit var updateWhatIcons: List<ImageView>

    private lateinit var adaptWhatIcons: List<ImageView>

    private fun startIconPreview(icon: Int, name: String){
        val intent = Intent(context!!, ImageDialog::class.java)
        intent.putExtra("icon",icon)
        intent.putExtra("name",name)
        startActivity(intent)
    }

    private var mHandler= @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what) {
                1 ->{
                    whatsNewAdaption.visibility = View.VISIBLE
                    newNumber.text = allAdaptions.size.toString()

                    for ((index, value) in allAdaptions.withIndex()){
                        if (index == 5){

                            loadAndAnimIcon(R.drawable.ic_more, updateWhatIcons6)

                            updateWhatIcons6.setOnClickListener {
                                IconsFragmentDialog.show(this@HomeFragment.childFragmentManager, "UpdateIconDialog","更新了哪些图标" , allAdaptions)
                            }
                            break
                        }

                        loadAndAnimIcon(context!!.resources.getIdentifier(value.icon,"drawable",context!!.packageName), updateWhatIcons[index])
                        updateWhatIcons[index].setOnClickListener {
                            startIconPreview(context!!.resources.getIdentifier(value.icon,"drawable",context!!.packageName), value.icon)
                        }
                    }

                }
                2 ->{
                    newNumber.text = adaptationsNew.size.toString()
                    whatsNewAdaption.visibility = View.VISIBLE

                    for ((index, value) in adaptationsNew.withIndex()){
                        if (index == 5){

                            loadAndAnimIcon(R.drawable.ic_more, updateWhatIcons6)

                            updateWhatIcons6.setOnClickListener {
                                IconsFragmentDialog.show(this@HomeFragment.childFragmentManager, "UpdateIconDialog","更新了哪些图标" , adaptationsNew)
                            }

                            break
                        }

                        loadAndAnimIcon(context!!.resources.getIdentifier(value.icon,"drawable",context!!.packageName), updateWhatIcons[index])
                        updateWhatIcons[index].setOnClickListener {
                            startIconPreview(context!!.resources.getIdentifier(value.icon,"drawable",context!!.packageName), value.icon)
                        }
                    }

                }
                3 ->{
                    whatsAdaption.visibility = View.VISIBLE

                    for ((index, value) in newAdaptions.withIndex()){
                        if (index == 5){

                            loadAndAnimIcon(R.drawable.ic_more, adaptWhatIcons6)

                            adaptWhatIcons6.setOnClickListener {
                                IconsFragmentDialog.show(this@HomeFragment.childFragmentManager, "UpdateIconDialog","新适配设备上哪些图标" , newAdaptions)
                            }

                            break
                        }

                        loadAndAnimIcon(context!!.resources.getIdentifier(value.icon,"drawable",context!!.packageName), adaptWhatIcons[index])
                        adaptWhatIcons[index].setOnClickListener {
                            startIconPreview(context!!.resources.getIdentifier(value.icon,"drawable",context!!.packageName), value.icon)
                        }
                    }
                    textView.text = "对本设备新适配了${newAdaptions.size}应用"
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    //开始加载View
    private fun initView(){
        title.text = resources.getString(R.string.home_title)
//        newNumber.text = resources.getString(R.string.new_icons)
        Glide.with(this).load(R.drawable.logo).into(logo)
        updateWhatIcons = listOf<ImageView>(updateWhatIcons1, updateWhatIcons2, updateWhatIcons3, updateWhatIcons4, updateWhatIcons5)
        adaptWhatIcons = listOf<ImageView>(adaptWhatIcons1, adaptWhatIcons2, adaptWhatIcons3, adaptWhatIcons4, adaptWhatIcons5)

//        startIntentUrl(INTENT_URL_FORMAT.replace("{payCode}", resources.getString(R.string.alipay_code)))

        iconsPage.setOnClickListener {
            callbacks.callback(1)
        }

        newDialog.setOnClickListener {
            callbacks.callback(2)
        }

        openResource.setOnClickListener {
            startHttp(resources.getString(R.string.home_link))
        }

        doAsyncTask {
            parser()
            if (isDestroyed){
                onUI {
                    iconNumber.text = adaptations.toString()
                }
            }
        }

        card.setOnClickListener {
            openAppStore(packageName = context!!.packageName)
        }

        donate.setOnClickListener {
            startIntentUrl(INTENT_URL_FORMAT.replace("{payCode}", resources.getString(R.string.alipay_code)))
        }


        adaptationsList = (activity!! as MainActivity).adaptations
        showUpdateIcons(adaptationsList)
    }

    private fun showUpdateIcons(adaptationsList: ArrayList<AdaptionBean>) {

        doAsyncTask {
            if (File(context!!.filesDir, "appfilter.xml").exists()){
                //获取旧版数据
                if (parser2().isNotEmpty()){
                    for (v in adaptationsList) {
                        if (checkIndexOf(adaptationsOld, v) < 0){
                            adaptationsNew.add(v)
                        }
                    }
                    //相对于旧版更新了哪些图标
                    getNewIcons(adaptationsNew)
                }else{
                    //第一次安装直接显示所有图标
                    showAll(this@HomeFragment.adaptationsList)
                }
            }
        }
    }

    private fun loadAndAnimIcon(drawable: Int, icon: ImageView){

        icon.visibility = View.INVISIBLE

        Glide.with(this).load(drawable).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                icon.post {
                    iconAnimator(icon)
                }
                return false
            }

        }).into(icon)
    }

    private fun iconAnimator(v: View){

        val animator = ValueAnimator()
        animator.setFloatValues(0f, 1f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 600
        animator.addUpdateListener {
            v.scaleX = it.animatedValue as Float
            v.scaleY = it.animatedValue as Float
        }
        animator.start()
        v.visibility = View.VISIBLE
    }

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
    private fun parser2(): String{

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
                                    adaptationsOld.add(AdaptionBean(pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/")), pkgActivity.substring(pkgActivity.indexOf("/")+1,pkgActivity.indexOf("}")),xml.getAttributeValue(0)))
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

    /**
     * 更新了哪些图标(直接展示所有图标)
     */
    private fun showAll(iconsList: ArrayList<AdaptionBean>){

//        if (allAdaptions.size < 1) {
//            parser()
//        }
        if (iconsList.size > 0 && isDestroyed){
            val msg = Message()
            msg.what = 1
            mHandler.sendMessage(msg)
        }else{
            return
        }

        showAdaptions(iconsList)
    }

    /**
     * 更新了哪些图标
     */
    private fun getNewIcons(iconsList: ArrayList<AdaptionBean>){
        if (iconsList.size > 0 && isDestroyed){
            val msg = Message()
            msg.what = 2
            mHandler.sendMessage(msg)
        }else{
            return
        }

        showAdaptions(iconsList)
    }

    /**
     * 新适配设备上哪些图标
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

        if (newAdaptions.size > 0 && isDestroyed){
            val msg = Message()
            msg.what = 3
            mHandler.sendMessage(msg)
        }
    }

    private fun parser(){
        adaptations = 0
        val xml = context!!.resources.getXml(R.xml.drawable)
        var type = xml.eventType
        try {
            while (type != XmlPullParser.END_DOCUMENT){
                when(type){
                    XmlPullParser.START_TAG ->{
                        if (xml.name == "item"){

                            val drawableString = xml.getAttributeValue(0)
                            allAdaptions.add(AdaptionBean("", "", drawableString))

                            adaptations++
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


    private fun startHttp(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)
        context!!.startActivity(intent)
    }


    private fun openAppStore(packageName: String) {
        try {
            val uri = Uri.parse("https://coolapk.com/apk/$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, resources.getString(R.string.no_store), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    interface Callbacks {
        fun callback(position: Int)
    }

    private lateinit var callbacks: Callbacks

    fun setCallbackListener(callbacks: Callbacks) {
        this.callbacks = callbacks
    }


    private fun startIntentUrl(intentFullUrl: String): Boolean {

        if (hasInstalledAlipayClient(context!!)){
            try {
                val intent = Intent.parseUri(
                    intentFullUrl,
                    Intent.URI_INTENT_SCHEME
                )
                startActivity(intent)
                return true
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                return false
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                return false
            }
        }else{
            Toast.makeText(context, "未安装支付宝", Toast.LENGTH_SHORT).show()
        }

        return false
    }

    /**
     * 判断支付宝客户端是否已安装，建议调用转账前检查
     * @param context Context
     * @return 支付宝客户端是否已安装
     */
    private fun hasInstalledAlipayClient(context: Context): Boolean{
        val pm = context.packageManager
        try {
            val info = pm.getPackageInfo(ALIPAY_PACKAGE_NAME, 0)
            return info != null
        } catch (e: PackageManager.NameNotFoundException ) {
            e.printStackTrace()
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }
}
