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
import de.hdodenhof.circleimageview.CircleImageView
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import jp.wasabeef.glide.transformations.BlurTransformation
import org.andcreator.iconpack.util.Utils


class AboutAdapter(private val context: Context,
                   private val credits: ArrayList<AboutBean>) : RecyclerView.Adapter<AboutAdapter.AboutHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AboutHolder {
        return AboutHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_detailed_credit, p0, false))
    }

    override fun getItemCount(): Int {
        return credits.size
    }

    override fun onBindViewHolder(p0: AboutHolder, p1: Int) {
        val bean = credits[p1]
        Glide.with(context)
            .load(bean.banner)
            .apply(bitmapTransform(BlurTransformation(25)))
            .into(p0.banner)

        Glide.with(context).load(bean.photo).into(p0.photo)
        p0.title.text = bean.title
        p0.content.text = bean.context

        if (bean.buttons.size > 0){
            p0.buttons.setButtonCount(bean.buttons.size)
            if (!p0.buttons.hasAllButtons()){
                if (bean.buttons.size != bean.links.size){
                    throw IllegalStateException(
                        "Button names and button links must have the same number of items" + "."
                    )
                }

                for ((index,value) in bean.buttons.withIndex()){
                    p0.buttons.addButton(value,bean.links[index])
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
        if (bean.egg.isNotEmpty()) {
            if (bean.egg == "debug") {
                p0.photo.setOnLongClickListener(object : View.OnLongClickListener {
                    override fun onLongClick(p0: View?): Boolean {
                        enableDebug()
                        return false
                    }
                })
            }
        }
    }
    
    private fun enableDebug() {
        Utils.setDebug(true, context)
        Toast.makeText(context, context.getString(R.string.debug_enabled), Toast.LENGTH_SHORT).show()
    }

    //打开链接
    private fun startHttp(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)
        context.startActivity(intent)
    }

    class AboutHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var banner: ImageView = itemView.findViewById(R.id.banner)
        var photo: CircleImageView = itemView.findViewById(R.id.photo)
        var title: TextView = itemView.findViewById(R.id.title)
        var content: TextView = itemView.findViewById(R.id.content)
        var buttons: SplitButtonsLayout = itemView.findViewById(R.id.buttons)

    }
}