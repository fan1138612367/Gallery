package com.example.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.gallery.databinding.GalleryCellBinding
import com.example.gallery.databinding.GalleryFooterBinding

class GalleryAdapter : PagingDataAdapter<PhotoItem, PixabayViewHolder>(DiffCallback) {
    override fun onBindViewHolder(holder: PixabayViewHolder, position: Int) {
        val photoItem = getItem(position)
        if (photoItem != null) {
            holder.viewBinding.apply {
                shimmerLayoutCell.apply {
                    setShimmerColor(0x55FFFFFF)
                    setShimmerAngle(30)
                    startShimmerAnimation()
                }
                textViewUser.text = photoItem.photoUser
                textViewLikes.text = photoItem.photoLikes.toString()
                textViewFavorites.text = photoItem.photoFavorites.toString()
                imageView.layoutParams.height = photoItem.photoHeight
                Glide.with(holder.itemView)
                    .load(photoItem.previewURL)
                    .placeholder(R.drawable.photo_placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false.also { shimmerLayoutCell.stopShimmerAnimation() }
                        }
                    }).into(imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PixabayViewHolder {
        val holder = PixabayViewHolder(
            GalleryCellBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        holder.itemView.setOnClickListener {
            Bundle().apply {
                putInt("PHOTO_POSITION", holder.absoluteAdapterPosition)    //传递当前图片位置
                it.findNavController()
                    .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)
            }
        }
        return holder
    }

    object DiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem) =
            oldItem.photoId == newItem.photoId

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem) = oldItem == newItem
    }
}

class PixabayViewHolder(val viewBinding: GalleryCellBinding) :
    RecyclerView.ViewHolder(viewBinding.root)

class FooterAdapter(private val retry: () -> Unit) : LoadStateAdapter<FooterViewHolder>() {
    override fun onBindViewHolder(holder: FooterViewHolder, loadState: LoadState) {
        holder.viewBinding.apply {
            when (loadState) {
                is LoadState.Loading -> {
                    textView.text = "正在加载"
                    progressBar.visibility = View.VISIBLE
                    holder.itemView.isClickable = false
                }
                is LoadState.Error -> {
                    textView.text = "加载出错，点击重试"
                    progressBar.visibility = View.GONE
                    holder.itemView.isClickable = true
                }
                else -> {
                    textView.text = "加载完毕"
                    progressBar.visibility = View.GONE
                    holder.itemView.isClickable = false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): FooterViewHolder {
        val holder = FooterViewHolder(
            GalleryFooterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        holder.itemView.apply {
            (layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
            setOnClickListener {
                retry()
            }
        }
        return holder
    }
}

class FooterViewHolder(val viewBinding: GalleryFooterBinding) :
    RecyclerView.ViewHolder(viewBinding.root)