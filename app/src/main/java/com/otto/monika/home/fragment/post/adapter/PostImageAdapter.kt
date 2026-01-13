package com.otto.monika.home.fragment.post.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.otto.monika.R

/**
 * 帖子图片列表适配器
 * 用于显示帖子中的图片（三列网格布局）
 */
class PostImageAdapter(imageList: List<String> = emptyList()) :
    RecyclerView.Adapter<PostImageAdapter.ImageViewHolder>() {

    private var imageList: List<String> = imageList
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onImagesClick: ((imageList: List<String>, index: Int) -> Unit)? = null

    /**
     * 更新数据
     */
    fun updateData(newImageList: List<String>) {
        imageList = newImageList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_image_grid, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (position < imageList.size) {
            holder.bind(imageList[position], position)
        }
    }

    override fun getItemCount(): Int = imageList.size

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_post_image_grid)

        fun bind(imageUrl: String, position: Int) {
            if (imageUrl.isNotEmpty()) {
                Glide.with(imageView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.generic_avatar_default)
            }
            imageView.setOnClickListener {
                onImagesClick?.invoke(imageList,position)
            }

        }
    }
}

