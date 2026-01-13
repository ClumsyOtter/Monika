package com.otto.monika.post.publish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.otto.monika.R

/**
 * 发布动态图片列表适配器
 */
class PublishImageAdapter() : RecyclerView.Adapter<PublishImageAdapter.ViewHolder>() {
    val imageList: MutableList<FileObject> = mutableListOf()

    // 添加按钮点击回调
    var onAddImageClickListener: (() -> Unit)? = null
    var onDeleteClickListener: ((Int) -> Unit)? = null

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_IMAGE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_ADD
        } else {
            TYPE_IMAGE
        }
    }

    fun setData(images: List<FileObject>) {
        imageList.clear()
        imageList.addAll(images)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_publish_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        if (viewType == TYPE_ADD) {
            // 显示添加按钮（永远在第一个位置）
            holder.ivImage.isVisible = false
            holder.ivDelete.visibility = View.GONE
            holder.ivAdd.isVisible = true
            holder.itemView.setOnClickListener {
                onAddImageClickListener?.invoke()
            }
        } else {
            // 显示图片（position从1开始，所以实际图片索引是position - 1）
            holder.ivImage.isVisible = true
            holder.ivDelete.isVisible = true
            holder.ivAdd.isVisible = false
            val imageIndex = position - 1 // 因为Add按钮在position 0，所以图片索引需要减1
            val imagePath = imageList[imageIndex].localUrl
            Glide.with(holder.itemView.context).load(imagePath).into(holder.ivImage)
            holder.ivDelete.setOnClickListener {
                onDeleteClickListener?.invoke(imageIndex) // 传递实际图片索引
            }
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        // Add按钮永远在第一个位置，所以总数 = 1（Add按钮） + 图片数量
        // 如果图片数量达到12张，则不显示Add按钮
        return if (imageList.size >= 12) {
            imageList.size // 只显示图片，不显示Add按钮
        } else {
            1 + imageList.size // Add按钮 + 图片
        }
    }

    /**
     * 添加图片
     * @param imagePath 图片路径
     */
    fun addImage(imagePath: String) {
        if (imageList.size >= 12) {
            return
        }
        // 插入到第一个位置（Add按钮后面），这样用户可以看到刚刚添加的图片
        imageList.add(FileObject().apply { localUrl = imagePath })
        notifyDataSetChanged()
    }

    /**
     * 删除图片
     * @param position 图片在imageList中的索引（不是RecyclerView的position）
     */
    fun removeImage(position: Int) {
        if (position < 0 || position >= imageList.size) {
            return
        }
        imageList.removeAt(position)
        // 通知删除图片（Add按钮在position 0，所以RecyclerView的position是position + 1）
        notifyItemRemoved(position + 1)
        // 通知后续item位置变化
        if (position < imageList.size) {
            notifyItemRangeChanged(position + 1, imageList.size - position)
        }
    }

    /**
     * 获取图片数量
     */
    fun getImageCount(): Int = imageList.size

    /**
     * 是否可以添加更多图片
     */
    fun canAddMore(): Boolean = imageList.size < 12


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_publish_image)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_publish_image_delete)
        val ivAdd: ImageView = itemView.findViewById(R.id.iv_publish_image_add)
    }
}

