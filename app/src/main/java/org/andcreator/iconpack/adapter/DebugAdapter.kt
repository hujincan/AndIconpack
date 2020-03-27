package org.andcreator.iconpack.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import org.andcreator.iconpack.R
import org.andcreator.iconpack.bean.RequestsBean
import org.andcreator.iconpack.util.Utils
import org.andcreator.iconpack.view.FastScrollRecyclerView
import java.util.*
import kotlin.collections.ArrayList

class DebugAdapter(private val context: Context,
                   private var dataList: ArrayList<RequestsBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    override fun getSectionName(position: Int): String {
        return if (position > 0){
            dataList[position].name!!.substring(0, 1).toUpperCase(Locale.ENGLISH)
        }else{
            ""
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        when (p1){
            0 ->{
                return RequestHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_debug_app_info, p0, false))
            }
            1 ->{
                return RequestHolderHead(LayoutInflater.from(p0.context).inflate(R.layout.header_debug, p0, false))
            }
        }

        return RequestHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_debug_app_info, p0, false))
    }

    override fun getItemCount(): Int {

        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val bean = dataList[p1]

        when (p0.itemViewType){
            0, 2 ->{
                val holder: RequestHolder = p0 as RequestHolder
                Glide.with(context).load(bean.icon).into(holder.icon)
                holder.appName.text = bean.name
                val info = bean.pagName + "/" + bean.activityName
                holder.appInfo.text = info
                holder.body.setOnLongClickListener {
                    Toast.makeText(context,context.getString(R.string.apk_info_copied), Toast.LENGTH_SHORT).show()
                    Utils.copy(holder.appInfo.text.toString(), context)
                    false
                }
            }
        }
    }

    class RequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var body: LinearLayout = itemView.findViewById(R.id.debug_body);
        var icon: ImageView = itemView.findViewById(R.id.debug_icon)
        var appName: TextView = itemView.findViewById(R.id.debug_appName)
        var appInfo: TextView = itemView.findViewById(R.id.debug_appInfo)
    }

    class RequestHolderHead(itemView: View) : RecyclerView.ViewHolder(itemView){
        var adaptation: TextView = itemView.findViewById(R.id.header_debug)
    }
}