package org.andcreator.iconpack.fragment


import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_update.*

import org.andcreator.iconpack.R
import org.andcreator.iconpack.util.DisplayUtil
import org.andcreator.iconpack.util.Utils

/**
 * A simple [Fragment] subclass.
 *
 */
class UpdateFragment: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init(){
        close.setOnClickListener {
            dismiss()
        }

        appVersion.text = "${requireContext().resources.getString(R.string.official)} ${Utils.getAppVersionName(requireContext())}"
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
