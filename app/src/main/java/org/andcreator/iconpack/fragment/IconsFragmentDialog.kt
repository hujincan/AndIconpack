package org.andcreator.iconpack.fragment

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_icons_dialog.*
import org.andcreator.iconpack.R
import org.andcreator.iconpack.activity.ImageDialog
import org.andcreator.iconpack.adapter.HomeIconsAdapter
import org.andcreator.iconpack.bean.AdaptionBean
import org.andcreator.iconpack.util.DisplayUtil

class IconsFragmentDialog private constructor():  DialogFragment() {

    private lateinit var iconList: ArrayList<AdaptionBean>

    private var titleText = "图标"

    companion object {
        fun show(supportFragmentManager: FragmentManager, tag: String, titleText: String, iconList: ArrayList<AdaptionBean>) {
            IconsFragmentDialog().apply {
                this.titleText = titleText
                this.iconList = iconList
            }.show(supportFragmentManager, tag)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_icons_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){

        title.text = titleText

        updateIconList.layoutManager = GridLayoutManager(context, 4)
        val adapter = HomeIconsAdapter(requireContext(), iconList)
        updateIconList.adapter = adapter

        adapter.setClickListener(object : HomeIconsAdapter.OnItemClickListener{
            override fun onClick(icon: Int, name: String) {
                val intent = Intent(context!!, ImageDialog::class.java)
                intent.putExtra("icon",icon)
                intent.putExtra("name",name)
                startActivity(intent)
            }
        })

        close.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog!!.window
        val windowParams = window!!.attributes
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics) //为获取屏幕宽高
        windowParams.width = kotlin.math.min(
            DisplayUtil.dip2px(context, 390f),
            (displayMetrics.widthPixels * 0.9).toInt()
        )

        window.attributes = windowParams
    }
}