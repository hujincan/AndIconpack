package org.andcreator.iconpack.fragment


import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.WallpaperManager
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
import android.net.Uri
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_home.title
import kotlinx.android.synthetic.main.fragment_icons.*
import kotlinx.android.synthetic.main.item_designer.*
import kotlinx.android.synthetic.main.rate_layout.*
import org.andcreator.iconpack.activity.ImageDialog
import org.andcreator.iconpack.bean.IconsBean
import org.andcreator.iconpack.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : BaseFragment() {

    private lateinit var updateWhatIcons: List<ImageView>

    private lateinit var adaptWhatIcons: List<ImageView>

    /**
     * 获取未授权的权限
     */
    private lateinit var permissionList: MutableList<String>

    /**
     * 请求权限的返回值
     */
    private val permissionCode = 1

    private fun startIconPreview(icon: Int, name: String){
        val intent = Intent(requireContext(), ImageDialog::class.java)
        intent.putExtra("icon",icon)
        intent.putExtra("name",name)
        startActivity(intent)
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
        view.setPadding(0, Utils.getStatusBarHeight(requireContext()), 0, 0)
        getPermission()
        initView()
    }

    //开始加载View
    private fun initView(){

        title.text = resources.getString(R.string.home_title)
        updateWhatIcons = listOf<ImageView>(updateWhatIcons1, updateWhatIcons2, updateWhatIcons3, updateWhatIcons4)
        adaptWhatIcons = listOf<ImageView>(adaptWhatIcons1, adaptWhatIcons2, adaptWhatIcons3, adaptWhatIcons4)

        iconsPage.setOnClickListener {
            callbacks.callback(1)
        }

        newDialog.setOnClickListener {
            callbacks.callback(2)
        }

        // 得到所有drawable文件里的图标
        if (isDestroyed){
            AppAdaptationHelper.setContext(requireContext()).getRandomIcon {
                loadAndAnimIcon(it[0], icon1)
                loadAndAnimIcon(it[1], icon2)
                loadAndAnimIcon(it[2], icon3)
                loadAndAnimIcon(it[3], icon4)
            }.getUpdateIcon {

                if (it.size > 0){
                    val newIcon = it
                    whatsNewAdaption.visibility = View.VISIBLE
                    updateWhatIconsLayout.visibility = View.VISIBLE
                    newNumber.text = newIcon.size.toString()

                    for ((index, value) in newIcon.withIndex()){
                        if (index == 4){
                            loadAndAnimIcon(R.drawable.ic_more, updateWhatIcons5)
                            updateWhatIcons5.setOnClickListener {
                                IconsFragmentDialog.show(this@HomeFragment.childFragmentManager, "UpdateIconDialog","更新了哪些图标" , newIcon)
                            }
                            break
                        }
                        loadAndAnimIcon(requireContext().resources.getIdentifier(value.icon,"drawable", requireContext().packageName), updateWhatIcons[index])
                        updateWhatIcons[index].setOnClickListener {
                            startIconPreview(requireContext().resources.getIdentifier(value.icon,"drawable", requireContext().packageName), value.iconName)
                        }
                    }
                }

            }.getUpdateAdaptionIcon {

                if (it.size > 0) {

                    val newAdaption = it

                    whatsAdaption.visibility = View.VISIBLE
                    adaptWhatIconsLayout.visibility = View.VISIBLE

                    for ((index, value) in newAdaption.withIndex()){
                        if (index == 4){
                            loadAndAnimIcon(R.drawable.ic_more, adaptWhatIcons5)
                            adaptWhatIcons5.setOnClickListener {
                                IconsFragmentDialog.show(this@HomeFragment.childFragmentManager, "UpdateIconDialog","新适配设备上哪些图标" , newAdaption)
                            }
                            break
                        }
                        loadAndAnimIcon(requireContext().resources.getIdentifier(value.icon,"drawable",requireContext().packageName), adaptWhatIcons[index])
                        adaptWhatIcons[index].setOnClickListener {
                            startIconPreview(requireContext().resources.getIdentifier(value.icon,"drawable",requireContext().packageName), value.iconName)
                        }
                    }
                    whatsAdaption.text = "对设备新适配${newAdaption.size}应用"

                }
            }.getIconCount {
                iconNumber.text = it.toString()
            }
        }

        val links = resources.getStringArray(R.array.links)
        if (links.isNotEmpty()){
            for (value in links) {
                val values = value.split("$$")
                val view = LayoutInflater.from(context).inflate(R.layout.rate_layout, linksLayout, false)
                linksLayout.addView(view)

                Glide.with(this).load(resources.getIdentifier(values[0],"drawable", this.requireContext().packageName)).into(view.findViewById<ImageView>(R.id.logo))
                view.findViewById<TextView>(R.id.idTitle).text = values[1]
                view.findViewById<TextView>(R.id.idSubTitle).text = values[2]

                if (values[3] == "rate") {
                    view.setOnClickListener {
                        openAppStore(packageName = requireContext().packageName)
                    }
                } else {
                    view.setOnClickListener {
                        startHttp(values[3])
                    }
                }
            }
        }
    }

    private fun loadAndAnimIcon(drawable: Int, icon: ImageView){
        icon.visibility = View.INVISIBLE
        Glide.with(icon).load(drawable).into(icon)
        icon.post {
            iconAnimator(icon)
        }
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
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                v.visibility = View.VISIBLE
            }
        })
        animator.start()
    }

    private fun openAppStore(packageName: String) {
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            requireContext().startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, resources.getString(R.string.no_store), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    /**
     * 获取必要权限
     */
    private fun getPermission(){
        val permission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionList = mutableListOf()
        permissionList.clear()

        //获取未授权的权限
        for (permiss:String in permission){
            if (ContextCompat.checkSelfPermission(requireContext(), permiss) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permiss)
            }
        }
        if (permissionList.isNotEmpty()){
            //请求权限方法
            val permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(requireActivity(), permissions, permissionCode)
        }else{
            checkWallpaper(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            permissionCode ->{
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    checkWallpaper(false)
                }else{
                    permissionList.clear()
                    checkWallpaper(true)
                }
            }
            else ->{}
        }
    }

    /**
     * 检索壁纸颜色
     */
    private fun checkWallpaper(loadWallpaper: Boolean) {
        if (loadWallpaper) {
            val wallpaperManager = WallpaperManager.getInstance(this.context)
            headImg.setImageDrawable(wallpaperManager.drawable)
            UnboundedImageViewHelper.with(headImg).addClickListener(object : UnboundedImageViewHelper.ClickListener {
                override fun onClick(view: View, count: Int) {
                    if (count < 2) {
                        AppAdaptationHelper.setContext(requireActivity()).getRandomIcon {
                            loadAndAnimIcon(it[0], icon1)
                            loadAndAnimIcon(it[1], icon2)
                            loadAndAnimIcon(it[2], icon3)
                            loadAndAnimIcon(it[3], icon4)
                        }
                    }
                }
            })
        } else {
            headImg.setImageResource(R.drawable.banner_background)
            UnboundedImageViewHelper.with(headImg).addClickListener(object : UnboundedImageViewHelper.ClickListener {
                override fun onClick(view: View, count: Int) {
                    if (count < 2) {
                        AppAdaptationHelper.setContext(requireActivity()).getRandomIcon {
                            loadAndAnimIcon(it[0], icon1)
                            loadAndAnimIcon(it[1], icon2)
                            loadAndAnimIcon(it[2], icon3)
                            loadAndAnimIcon(it[3], icon4)
                        }
                    }
                }
            })
        }
    }

    interface Callbacks {
        fun callback(position: Int)
    }

    private lateinit var callbacks: Callbacks

    fun setCallbackListener(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    //打开链接
    private fun startHttp(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)
        requireContext().startActivity(intent)
    }
}
