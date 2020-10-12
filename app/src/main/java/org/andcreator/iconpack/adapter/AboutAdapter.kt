package org.andcreator.iconpack.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.andcreator.iconpack.R
import org.andcreator.iconpack.bean.AboutBean
import org.andcreator.iconpack.view.SplitButtonsLayout
import android.content.Intent
import android.net.Uri


class AboutAdapter(private val context: Context,
                   private val credits: ArrayList<AboutBean>) : RecyclerView.Adapter<AboutAdapter.AboutHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AboutHolder {
        return AboutHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_designer, p0, false))
    }

    override fun getItemCount(): Int {
        return credits.size
    }

    override fun onBindViewHolder(p0: AboutHolder, p1: Int) {
        val bean = credits[p1]
        Glide.with(p0.banner)
            .load(bean.banner)
//            .apply(bitmapTransform(BlurTransformation(25)))
            .into(p0.banner)

        Glide.with(p0.photo).load(bean.photo).into(p0.photo)
        p0.title.text = bean.title
        p0.content.text = bean.content

        if (bean.buttons.size > 0){
            p0.buttons.setButtonCount(bean.buttons.size)
            if (!p0.buttons.hasAllButtons()){
                if (bean.buttons.size != bean.links.size){
                    throw IllegalStateException(
                        "Button names and button links must have the same number of items" + "."
                    )
                }

                for ((index, value) in bean.buttons.withIndex()){
                    p0.buttons.addButton(value, bean.links[index])
                }
            }
        }else{
            p0.buttons.visibility = View.GONE
        }
        for (i in 0..p0.buttons.childCount){
            if ( p0.buttons.getChildAt(i) !=null){
                p0.buttons.getChildAt(i).setOnClickListener { v ->
                    if (v!!.tag is String) {
                        try {
                            startHttp(v.tag.toString())
                        } catch (e: Exception) {
                        }

                    }
                }
            }
        }
    }

    //打开链接
    private fun startHttp(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)
        context.startActivity(intent)
    }

    class AboutHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var banner: ImageView = itemView.findViewById(R.id.banner)
        var photo: ImageView = itemView.findViewById(R.id.photo)
        var content: TextView = itemView.findViewById(R.id.content)
        var title: TextView = itemView.findViewById(R.id.title)
        var buttons: SplitButtonsLayout = itemView.findViewById(R.id.buttons)


    }
}