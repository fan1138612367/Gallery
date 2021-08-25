package com.example.gallery

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.gallery.databinding.PagerPhotoViewBinding

class PagerPhotoAdapter : PagingDataAdapter<PhotoItem, PagerPhotoViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PagerPhotoViewHolder {   //实现成员，加载View
        return PagerPhotoViewHolder(
            PagerPhotoViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PagerPhotoViewHolder, position: Int) {    //实现成员，加载图片
        holder.viewBinding.shimmerLayout.apply {
            setShimmerColor(0x55FFFFFF)
            setShimmerAngle(30)
            startShimmerAnimation()
        }
        Glide.with(holder.itemView) //Glide加载图片
            .load(getItem(position)?.fullURL)    //加载大图
            .placeholder(R.drawable.photo_placeholder) //设置占位图
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(  //实现成员，加载失败
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(   //实现成员，加载成功
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false.also { holder.viewBinding.shimmerLayout.stopShimmerAnimation() }
                }

            })
            .into(holder.viewBinding.pagerPhoto)
    }

    object DiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean { //实现成员
            return oldItem.photoId == newItem.photoId   //判断Item是否相同
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {  //实现成员
            return oldItem == newItem   //判断内容是否相同
        }
    }
}

class PagerPhotoViewHolder(val viewBinding: PagerPhotoViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root)