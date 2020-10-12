package org.andcreator.iconpack.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.andcreator.iconpack.R
import org.andcreator.iconpack.bean.AdaptionBean

class HomeIconsAdapter(private val context: Context,
                       private var dataList: ArrayList<AdaptionBean>): RecyclerView.Adapter<HomeIconsAdapter.IconsHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): IconsHolder {
        return IconsHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_home_icons, p0, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(p0: IconsHolder, p1: Int) {
        val bean = dataList[p1]
        val icon = context.resources.getIdentifier(bean.icon,"drawable",context.packageName)
        Glide.with(p0.icon).load(icon).into(p0.icon)
        p0.icon.setOnClickListener {
            clickListener.onClick(icon ,bean.iconName)
        }
    }

    class IconsHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var icon: ImageView = itemView.findViewById(R.id.icons)
    }

    interface OnItemClickListener{
        fun onClick(icon: Int,name: String)
    }

    private lateinit var clickListener: OnItemClickListener

    fun setClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

}