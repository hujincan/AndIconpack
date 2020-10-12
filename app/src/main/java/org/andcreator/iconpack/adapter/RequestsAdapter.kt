package org.andcreator.iconpack.adapter

import android.content.Context
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import org.andcreator.iconpack.Constans.MAX_REQUEST_COUNT
import org.andcreator.iconpack.R
import org.andcreator.iconpack.bean.RequestsBean
import org.andcreator.iconpack.view.FastScrollRecyclerView
import java.util.*
import kotlin.collections.ArrayList

class RequestsAdapter(private val context: Context,
                      private var dataList: ArrayList<RequestsBean>,
                      private var checkRead: ArrayList<Boolean>) : RecyclerView.Adapter<RequestsAdapter.RequestHolder>(), FastScrollRecyclerView.SectionedAdapter {

    var count = 0

    override fun getSectionName(position: Int): String {
        return if (position > 0){
            dataList[position].name!!.substring(0, 1).toUpperCase(Locale.ENGLISH)
        }else{
            ""
        }
    }

    interface OnSelectListener {
        fun onSelected(size: Int)
    }

    private lateinit var selectListener: OnSelectListener

    fun setOnSelectListener(selectListener: OnSelectListener) {
        this.selectListener = selectListener
    }

    private var isSelect = false

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RequestHolder {
        return RequestHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_app_to_request, p0, false))
    }

    override fun getItemCount(): Int {

        return dataList.size
    }

    override fun onBindViewHolder(p0: RequestHolder, p1: Int) {

        val bean = dataList[p1]
        Glide.with(p0.imgIcon).load(bean.icon).into(p0.imgIcon)
        p0.txtName.text = bean.name

        p0.chkSelected.isChecked = checkRead[p1]

        p0.requestCard.setOnClickListener {
            count = 0
            for (position in checkRead) {
                if (position) {
                    count++
                }
            }

            if (count < MAX_REQUEST_COUNT) {

                checkRead[p1] = !p0.chkSelected.isChecked
                p0.chkSelected.isChecked = !p0.chkSelected.isChecked

                if (!checkRead[p1]) {
                    selectListener.onSelected(--count)
                } else {
                    selectListener.onSelected(++count)
                }
            } else {

                if (checkRead[p1]) {
                    checkRead[p1] = !p0.chkSelected.isChecked
                    p0.chkSelected.isChecked = !p0.chkSelected.isChecked
                    selectListener.onSelected(--count)
                } else {
                    Toast.makeText(context, "最多被允许选择 $MAX_REQUEST_COUNT 个应用", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun selectAll(){

        if (count < MAX_REQUEST_COUNT) {

            if (!isSelect) {
                for (i in 0 until checkRead.size){
                    if (!checkRead[i]) {
                        checkRead[i] = true
                        count++

                        if (count == MAX_REQUEST_COUNT) break
                    }
                }
                isSelect = true
                selectListener.onSelected(count)
                notifyDataSetChanged()
            } else {
                for (i in 0 until checkRead.size){
                    checkRead[i] = false
                }
                count = 0
                isSelect = false
                selectListener.onSelected(0)
                notifyDataSetChanged()
            }
        } else if (count >= MAX_REQUEST_COUNT) {
            for (i in 0 until checkRead.size){
                checkRead[i] = false
            }
            count = 0
            isSelect = false
            selectListener.onSelected(0)
            notifyDataSetChanged()
        }
    }

    fun getSelect(): ArrayList<Boolean>{
        return checkRead
    }

    class RequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        var txtName: TextView = itemView.findViewById(R.id.txtName)
        var chkSelected: AppCompatCheckBox = itemView.findViewById(R.id.chkSelected)
        var requestCard:LinearLayout = itemView.findViewById(R.id.requestCard)
    }
}