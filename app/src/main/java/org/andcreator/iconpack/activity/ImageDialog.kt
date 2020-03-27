package org.andcreator.iconpack.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import org.andcreator.iconpack.R
import kotlinx.android.synthetic.main.activity_image_dialog.*
import org.andcreator.iconpack.util.OtherUtil
import org.andcreator.iconpack.util.Utils
import java.io.IOException

class ImageDialog : AppCompatActivity() {
    private lateinit var agreementDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_dialog)
        /*
        val m = windowManager
        val d = m.defaultDisplay //为获取屏幕宽高
        val p = window.attributes
//        p.height = (int) (d.getHeight()*0.9); //高度设置为屏幕的0.8
        p.width = d.width * 1 //宽度设置为屏幕的0.8
        window.attributes = p
        */
        initView()
    }

    private fun initView(){
        val image = intent.getIntExtra("icon",R.mipmap.ic_launcher)
        val iconName = intent.getStringExtra("name")
        Glide.with(this).load(image).into(icon)
        name.text = iconName

        close.setOnClickListener {
            finish()
        }

        save.setOnClickListener { view -> saveIcon(getDrawable(image)!!, iconName!!, view!!.context) }


        agreementDialog = AlertDialog.Builder(this, R.style.agreement_dialog)
            .setIcon(getDrawable(R.drawable.ic_warning))
            .setTitle(getString(R.string.agreement))
            .setMessage(getString(R.string.agreement_detail))
            .setNegativeButton(getString(R.string.deny)) { p0, _ ->
                Utils.setAgreementStatus(false, this)
                p0.dismiss()
                finish()
            }
            .setPositiveButton(getString(R.string.accept)) { p0, _ ->
                Utils.setAgreementStatus(true, this)
                p0.dismiss()
            }
            .create()
    }

    private fun saveIcon(icon: Drawable, name: String, context: Context) {

        if (!OtherUtil.isExternalStorageWritable()) {
            Toast.makeText(context, "没有获取储存权限，无法保存图片", Toast.LENGTH_SHORT).show()
        }

        if (!Utils.isShowAgreement(context)) {
            agreementDialog.show()
            return
        }

        val bmp = getBitmapFromDrawable(icon)
        try {
            Utils.saveBitmap(context, bmp, Bitmap.CompressFormat.PNG, "image/png", name)
            Toast.makeText(context, "图片 $name 保存成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("ImageDialog", e.message!!)
            Toast.makeText(context, "图片保存失败", Toast.LENGTH_SHORT).show()
        } finally {
            finish()
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }
}
