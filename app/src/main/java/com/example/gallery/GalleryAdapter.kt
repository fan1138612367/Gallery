package com.example.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.gallery.databinding.GalleryCellBinding

class GalleryAdapter : ListAdapter<PhotoItem, MyViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {   //实现成员，加载View
        val holder = MyViewHolder(
            GalleryCellBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        holder.itemView.setOnClickListener {
            Bundle().apply {
                putParcelableArrayList("PHOTO_LIST", ArrayList(currentList))    //传递整个List
                putInt("PHOTO_POSITION", holder.adapterPosition)    //传递当前图片位置
                holder.itemView.findNavController()
                    .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {    //实现成员，加载图片
        with(holder.viewBinding) {
            shimmerLayoutCell.apply {   //设置闪动
                setShimmerColor(0x55FFFFFF) //闪动颜色
                setShimmerAngle(0)  //闪动角度
                startShimmerAnimation() //开始闪动
            }
            textViewUser.text = getItem(position).photoUser
            textViewLikes.text = getItem(position).photoLikes.toString()
            textViewFavorites.text = getItem(position).photoFavorites.toString()
            imageView.layoutParams.height = getItem(position).photoHeight   //设置图片高度
        }
        Glide.with(holder.itemView) //Glide加载图片
            .load(getItem(position).previewURL) //加载预览图
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
                    return false.also { holder.viewBinding.shimmerLayoutCell.stopShimmerAnimation() }   //停止闪动
                }
            })
            .into(holder.viewBinding.imageView)
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

class MyViewHolder(val viewBinding: GalleryCellBinding) :
    RecyclerView.ViewHolder(viewBinding.root)   //自定义ViewHolder