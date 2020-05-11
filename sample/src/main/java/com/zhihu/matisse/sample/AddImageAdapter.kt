package com.zhihu.matisse.sample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zhihu.matisse.sample.WxPhotoActivity.Companion.MAX_SELECTED
import java.util.*

/**
 *@Author : yancheng
 *@Date : 2020/5/6
 *@Time : 17:56
 *@Describe ：
 **/
class AddImageAdapter(mContext : Context, layoutResId: Int,mutableList: MutableList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>( layoutResId,mutableList) {



    //最多9张图片
    private val maxImgCount = MAX_SELECTED
    //是否需要添加最后一张add图片
    private var  isAdded = true
    private var mData = mutableListOf<String>()
    private var clickListener :OnRecyclerViewItemClickListener? = null
    private var imageHigth = 0
    private var mInflater: LayoutInflater? = null

    init {
        mInflater = LayoutInflater.from(mContext)
        imageHigth =
            ((ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(70f)) / 3)
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        val ivImage =  holder.getView<ImageView>(R.id.iv_img)
        ivImage.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            imageHigth
        )

        if (isAdded && holder.layoutPosition == itemCount - 1) {
            ivImage.setImageResource(R.mipmap.icon_my_add)
            ivImage.setOnClickListener{
                clickListener?.onItemClick(ivImage,-1)
            }
        } else {
            if (item.startsWith("http")) {
                Glide.with(context).load(item).apply(
                    RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                ).into(ivImage)
            } else {
                Glide.with(context).load(item).centerCrop().into(ivImage)
            }
            ivImage.setOnClickListener{
                clickListener?.onItemClick(ivImage, holder.layoutPosition)
            }
        }
    }

    fun setRecycleClick(clickListener: OnRecyclerViewItemClickListener){
        this.clickListener = clickListener
    }


    fun setImageData(datas : List<String>){
        mData = ArrayList<String>(datas)
        isAdded = if (mData.size < maxImgCount) {
            mData.add("")
            true
        } else {
            false
        }
        setNewInstance(mData)
        notifyDataSetChanged()
    }

    fun getImages(): List<String> {
        //由于图片未选满时，最后一张显示添加图片，因此这个方法返回真正的已选图片
        return if (isAdded) ArrayList<String>(
            mData.subList(
                0,
                mData.size - 1
            )
        ) else mData
    }

    interface OnRecyclerViewItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

}
