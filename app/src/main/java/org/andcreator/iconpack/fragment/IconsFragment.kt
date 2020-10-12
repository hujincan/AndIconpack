package org.andcreator.iconpack.fragment



import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_icons.*
import org.andcreator.iconpack.R
import org.andcreator.iconpack.adapter.IconsAdapter
import org.andcreator.iconpack.bean.IconsBean
import org.andcreator.iconpack.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class IconsFragment : BaseFragment() {

    /**
     * 图标列表
     */
    private var iconsList = ArrayList<IconsBean>()

    /**
     * 搜索图标列表
     */
    private var searchIconsList = ArrayList<IconsBean>()

    /**
     * 图标列表适配器
     */
    private lateinit var adapter: IconsAdapter

    /**
     * 共享元素动画帮助类
     */
    private var shareElementHelper: ShareElementHelper = ShareElementHelper()

    // 图标默认颜色
    private var iconColor = -0x1

    private var isDark = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_icons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setPadding(0, Utils.getStatusBarHeight(requireContext()), 0, 0)
        isDark = ContextCompat.getColor(requireContext(), R.color.backgroundColor) != ContextCompat.getColor(requireContext(), R.color.white)
        iconColor = ContextCompat.getColor(requireContext(), R.color.backgroundColor)
        initView()
    }

    private fun initView(){

        recyclerIcons.layoutManager = GridLayoutManager(context, 4)
        adapter = IconsAdapter(requireContext(), iconsList)
        recyclerIcons.adapter = adapter
        recyclerIcons.addOnScrollListener(object : HideScrollListener() {})

        // 设置图标点击预览
        adapter.setClickListener(object : IconsAdapter.OnItemClickListener{
            override fun onClick(iconView: ImageView, icon: Int, name: String) {

                closeKeyboard()
                background.visibility = View.VISIBLE

                previewName.text = name

                if (isDark) {
                    setBackgroundColor(ContextCompat.getColor(context!!, R.color.backgroundColor), iconColor)
                } else {

                    val builder: Palette.Builder = Palette.from(drawableToBitmap(icon))
                    builder.generate { palette -> //获取到充满活力的这种色调
                        if (palette != null && palette.vibrantSwatch != null) {
                            iconColor = palette.vibrantSwatch!!.rgb
                        } else if (palette != null && palette.lightVibrantSwatch != null) {
                            iconColor = palette.lightVibrantSwatch!!.rgb
                        }else if (palette != null && palette.mutedSwatch != null) {
                            iconColor = palette.mutedSwatch!!.rgb
                        }else {
                            Toast.makeText(context, "获取不到颜色", Toast.LENGTH_SHORT).show()
                        }
                        setBackgroundColor(ContextCompat.getColor(context!!, R.color.backgroundColor), iconColor)
                    }
                }

                recyclerIcons.animate()
                    .setDuration(300L)
                    .alpha(0.1f)
                    .scaleX(0.94F)
                    .scaleY(0.94F)
                    .start()

                searchBar.animate()
                    .setDuration(300L)
                    .alpha(0.6F)
                    .start()

                shareElementHelper.with(icon, previewIcon, iconView).setStartListener(ValueAnimator.AnimatorUpdateListener {
                    phone.visibility = View.VISIBLE
                    previewName.visibility = View.VISIBLE
                    val animatedValue = it.animatedValue as Float
                    phone.translationY = phone.height - (phone.height * animatedValue)
                    phone.alpha = animatedValue
                    previewName.translationY = previewName.height - (previewName.height * animatedValue)
                    previewName.alpha = animatedValue

                }).setEndListener(ValueAnimator.AnimatorUpdateListener {
                    val animatedValue = it.animatedValue as Float
                    phone.translationY = phone.height * animatedValue
                    phone.alpha = 1 - animatedValue
                    previewName.translationY = previewName.height * animatedValue
                    previewName.alpha = 1 - animatedValue
                })
                onHide()
            }
        })

        background.setOnClickListener {
            cancelPreview()
        }

        // 加载图标
        reloadIcons()

        closeSearch.setOnClickListener {
            if (searchInput.text.isNotEmpty()){
                clearSearch()
            }
        }

        actionSearch.setOnClickListener {
            closeKeyboard()
            search(searchInput.text.toString())
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                search(p0.toString())
                Log.e("SearchTextChange1", p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.e("SearchTextChange2", p0.toString())
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.e("SearchTextChange3", p0.toString())
            }

        })
    }

    /**
     * 是否是窗口模式(分屏)
     */
    private fun isStatusBarVisible():Boolean {
        val isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && requireActivity().isInMultiWindowMode
        //窗口模式或者SDK小于19，不设置状态栏透明
        if (isInMultiWindowMode) {
            return false
        }
        return true
    }

    fun cancelPreview(): Boolean {

        if (background.visibility == View.VISIBLE) {
            shareElementHelper.setEndAnimatorListener(object : ShareElementHelper.EndListener {
                override fun onEnd() {
                    background.visibility = View.GONE
                }
            }).exitAnimator()
            setBackgroundColor(iconColor, ContextCompat.getColor(requireContext(), R.color.backgroundColor))

            recyclerIcons.animate()
                .setDuration(300L)
                .alpha(1f)
                .scaleX(1F)
                .scaleY(1F)
                .start()

            searchBar.animate()
                .setDuration(300L)
                .alpha(1F)
                .start()
            onShow()
            return true
        }

        return false
    }

    private fun setBackgroundColor(startColor: Int, endColor: Int) {
        val colorChange = ValueAnimator.ofFloat(0F, 1F)
        colorChange.duration = 300L
        colorChange.addUpdateListener {
            backgroundColor.setBackgroundColor(ColorUtil.getColor(startColor, endColor, it.animatedValue as Float))
        }
        colorChange.start()
    }

    private fun clearSearch() {
        searchInput.setText("")
        closeKeyboard()
        reloadIcons()
    }

    private fun closeKeyboard(){
        searchInput.clearFocus()
        val `in` = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        `in`.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }


    fun search(name: String) {
        if (name.isNotEmpty()){
            doAsyncTask {
                iconsList.clear()
                for (iconName in searchIconsList) {
                    if (iconName.name.contains(name, true)) {
                        iconsList.add(iconName)
                    }
                }
                if (isDestroyed){
                    onUI {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }else{
            reloadIcons()
        }
    }

    private fun reloadIcons() {
        if (isDestroyed){
            AppAdaptationHelper.setContext(requireContext()).getAdaptationIcon {
                iconsList.clear()
                iconsList.addAll(it)
                if (loading.visibility == View.VISIBLE){
                    loading.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
                searchIconsList.clear()
                searchIconsList = iconsList.clone() as ArrayList<IconsBean>
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

    private fun drawableToBitmap(id: Int): Bitmap {
        return BitmapFactory. decodeResource (resources, id)
    }
}
