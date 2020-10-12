package org.andcreator.iconpack.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.andcreator.iconpack.Constans
import org.andcreator.iconpack.LauncherIntents

import org.andcreator.iconpack.R
import org.andcreator.iconpack.adapter.LauncherAdapter
import org.andcreator.iconpack.bean.LauncherItem
import org.andcreator.iconpack.holder.LauncherHolder
import org.andcreator.iconpack.util.Utils
import kotlinx.android.synthetic.main.fragment_apply.*
import org.andcreator.iconpack.util.doAsyncTask
import org.andcreator.iconpack.util.onUI
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 *
 */
class ApplyFragment : BaseFragment() {

    private val launchers = ArrayList<LauncherItem>()
    private lateinit var adapter: LauncherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apply, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setPadding(0, Utils.getStatusBarHeight(requireContext()), 0, 0)
        initView()
    }

    private fun initView(){
        recyclerLaunchers.layoutManager = GridLayoutManager(context, 3)
        recyclerLaunchers.itemAnimator = DefaultItemAnimator()

        adapter = LauncherAdapter(requireContext(),launchers,object: LauncherHolder.OnLauncherClickListener{
            override fun onLauncherClick(item: LauncherItem) {
                when(item.name){
                    "Google Now" ->{
                        val appLink = Constans.MARKET_URL + resources.getString(R.string.extraapp)
                        AlertDialog.Builder(context!!)
                            .setTitle(resources.getString(R.string.gnl_title))
                            .setMessage(resources.getString(R.string.gnl_content))
                            .setPositiveButton(
                                resources.getString(R.string.download)
                        ) { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(appLink)
                            startActivity(intent)

                        }.setNeutralButton(resources.getString(R.string.cancel)){_,_->

                        }.show()
                    }
                    "LG Home" ->{
                        if (Utils.isAppInstalled(
                                context!!, item.packageName)) {
                            openLauncher(item.name)
                        } else {
                            AlertDialog.Builder(context!!)
                                .setMessage(resources.getString(R.string.lg_dialog_content))
                                .setPositiveButton(
                                    "Ok"
                                ) { _, _ -> }.show()
                        }
                    }
                    "CM Theme Engine" ->{
                        when {
                            Utils.isAppInstalled(context!!, "com.cyngn.theme.chooser") -> openLauncher("CM Theme Engine")
                            Utils.isAppInstalled(context!!, item.packageName) -> openLauncher(item.name)
                            else -> openInPlayStore(item)
                        }
                    }else ->{
                    if (Utils.isAppInstalled(context!!, item.packageName)){
                        openLauncher(item.name)
                    }else{
                        openInPlayStore(item)
                    }
                }
                }
            }

        })
        recyclerLaunchers.adapter = adapter

        doAsyncTask {
            getLaunchers()
            if(isDestroyed){
                onUI {
                    if (loading.visibility == View.VISIBLE){
                        loading.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun getLaunchers(): ArrayList<LauncherItem>{
        launchers.clear()
        val launcherArray = resources.getStringArray(R.array.launchers)
        val launcherColors = resources.getIntArray(R.array.launcher_colors)

        for ((index,value) in launcherArray.withIndex()){
            val s = value.split("|")
            launchers.add(LauncherItem(s[0],s[1],launcherColors[index]))
        }

        return launchers
    }


    private fun openLauncher(name: String) {
        val launcherName =
            Character.toUpperCase(name[0]) + name.substring(1).toLowerCase().replace(" ", "").replace("launcher", "")
        try {
            LauncherIntents(activity, launcherName)
        } catch (ex: IllegalArgumentException) {
            if (recyclerLaunchers != null) {
                Snackbar.make(recyclerLaunchers, R.string.no_launcher_intent, Snackbar.LENGTH_LONG).show()
            }
        }

    }

    private fun openInPlayStore(launcher: LauncherItem) {
        var intentString: String
        val launcherName = launcher.name
        val cmName = "CM Theme Engine"
        val dialogContent: String
        if (launcherName == cmName) {
            dialogContent = resources.getString(R.string.cm_dialog_content, launcher.name)
            intentString = "http://download.cyanogenmod.org/"
        } else {
            dialogContent = resources.getString(R.string.lni_content, launcher.name)
            intentString = Constans.MARKET_URL + launcher.packageName
        }

        AlertDialog.Builder(requireContext())
            .setTitle(launcher.name)
            .setMessage(dialogContent)
            .setPositiveButton(
                resources.getString(R.string.download)
            ) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(intentString)
                startActivity(intent)
            }.setNeutralButton(
                resources.getString(R.string.cancel)){_,_->

            }.show()
    }

}
