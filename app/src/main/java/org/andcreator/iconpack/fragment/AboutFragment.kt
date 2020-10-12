package org.andcreator.iconpack.fragment


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

import org.andcreator.iconpack.R
import kotlinx.android.synthetic.main.item_designer.*
import org.andcreator.iconpack.util.AppAdaptationHelper
import org.andcreator.iconpack.util.doAsyncTask
import org.andcreator.iconpack.util.onUI
import java.net.URISyntaxException
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 *
 */
class AboutFragment : BaseFragment() {

    var isDark = false

    private var progressInt = 0

    companion object{
        private const val ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone"

        private const val INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{payCode}%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        isDark = ContextCompat.getColor(requireContext(), R.color.backgroundColor) == ContextCompat.getColor(requireContext(), R.color.white)
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){

        if (resources.getString(R.string.alipay_code) != "false") {

            reward.shrink()
            reward.setOnClickListener {
                if (reward.isExtended) {
                    if (startIntentUrl(INTENT_URL_FORMAT.replace("{payCode}", resources.getString(R.string.alipay_code)))) {
                        Toast.makeText(requireContext(), "非常感谢您给予的动力支持!", Toast.LENGTH_SHORT).show()
                    } else {
                        reward.shrink()
                    }
                } else {
                    reward.extend()
                }
            }

            root.setOnClickListener {
                if (reward.isExtended) reward.shrink()
            }
        } else {
            reward.visibility = View.GONE
        }


        AppAdaptationHelper.setContext(requireContext()).getAdaptionCount {
            adaptationCount.text = "$it"
            if (progressInt == 0) {
                progressInt = it
            } else {
                progress.max = progressInt
                progress.progress = it
                val spannableString = SpannableString("感谢支持本作品，适配率 ${(it.toFloat() / progressInt.toFloat() * 100F).roundToInt()}%")
                spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorAccent)), spannableString.indexOf("率") + 1, spannableString.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                percent.text = spannableString
            }
        }.getAppCount {
            appCount.text = "$it"
            if (progressInt == 0) {
                progressInt = it
            } else {
                progress.max = it
                progress.progress = progressInt

                val spannableString = SpannableString("感谢支持本作品，适配率 ${(progressInt.toFloat() / it.toFloat() * 100F).roundToInt()}%")
                spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorAccent)), spannableString.indexOf("率") + 1, spannableString.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                percent.text = spannableString
            }
        }


        if (resources.getString(R.string.banner_background) != "false") {
            doAsyncTask {

                isDark = getBright(Bitmap.createScaledBitmap(drawableToBitmap(resources.getIdentifier(resources.getString(R.string.banner_background),"drawable", this.requireContext().packageName)), 500, 300, false)) > 220
                onUI {
//                callbacks.callback(isDark)
                    if (isDark){
                        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark))
                        content.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark))
                    }else{
                        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        content.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
            }
            Glide.with(banner)
                .load(resources.getIdentifier(resources.getString(R.string.banner_background),"drawable", this.requireContext().packageName))
//            .apply(bitmapTransform(BlurTransformation(25)))
                .into(banner)

        } else {
            if (isDark){
                title.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark))
                content.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark))
            } else {
                title.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                content.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        Glide.with(photo).load(R.drawable.author_and).into(photo)
        content.text = resources.getString(R.string.designer_message)

        val links = resources.getStringArray(R.array.author_links)
        if (links.isNotEmpty()){
            buttons.setButtonCount(links.size)
            if (!buttons.hasAllButtons()){
                for (value in links) {
                    val values = value.split("$$")
                    buttons.addButton(
                        resources.getIdentifier(values[0],"drawable", this.requireContext().packageName),
                        values[1]
                    )
                }
            }
        }else{
            buttons.visibility = View.GONE
        }
        for (i in 0..buttons.childCount){
            if ( buttons.getChildAt(i) !=null){
                buttons.getChildAt(i).setOnClickListener { v ->
                    if (v!!.tag is String) {
                        try {
                            startHttp(v.tag.toString())
                        } catch (e: Exception) { }
                    }
                }
            }
        }
    }

    //打开链接
    private fun startHttp(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)
        requireContext().startActivity(intent)
    }

    private fun getBright(bm: Bitmap): Int {
        val width = bm.width
        val height = bm.height
        var r: Int
        var g: Int
        var b: Int
        var count = 0
        var bright = 0
        for (i: Int in 0 until width) {
            for (j: Int in 0 until height) {
                count++
                val localTemp = bm.getPixel(i, j)
                r = localTemp.or(0xff00ffff.toInt()).shr(8).and(0x00ff)
                g = localTemp.or(0xffff00ff.toInt()).shr(8).and(0x0000ff)
                b = localTemp.or(0xffffff00.toInt()).and(0x0000ff)
                bright = (bright + 0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }
        }
        return bright/count
    }

    private fun drawableToBitmap(id: Int): Bitmap {
        return BitmapFactory. decodeResource (resources, id)
    }

    interface Callbacks {
        fun callback(isDark: Boolean)
    }

    private lateinit var callbacks: Callbacks

    fun setCallbackListener(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    private fun startIntentUrl(intentFullUrl: String): Boolean {

        if (hasInstalledAlipayClient(requireContext())){
            return try {
                val intent = Intent.parseUri(
                    intentFullUrl,
                    Intent.URI_INTENT_SCHEME
                )
                startActivity(intent)
                true
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                false
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                false
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
        return try {
            val info = pm.getPackageInfo(ALIPAY_PACKAGE_NAME, 0)
            info != null
        } catch (e: PackageManager.NameNotFoundException ) {
            e.printStackTrace()
            false
        }
    }

}
