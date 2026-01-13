package com.otto.monika.post.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.otto.monika.R

/**
 * 帖子图片列表 ViewPager Adapter
 */
class PostImagePagerAdapter(private val images: List<String>) : PagerAdapter() {

    override fun getCount(): Int = images.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    var onImageClicked: ((position: Int) -> Unit)? = null

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_post_image, container, false)

        val imageView: ImageView = itemView.findViewById(R.id.iv_post_image_item)
        val imageUrl = images[position]

        if (imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(R.drawable.generic_avatar_default)
                .error(R.drawable.generic_avatar_default)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.generic_avatar_default)
        }

        imageView.setOnClickListener {
            onImageClicked?.invoke(position)
        }

        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}

