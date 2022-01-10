package com.example.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.load
import com.example.gallery.databinding.GalleryCellBinding
import com.example.gallery.databinding.GalleryFooterBinding

class GalleryAdapter : PagingDataAdapter<PhotoItem, PixabayViewHolder>(
    object : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem) =
            oldItem.photoId == newItem.photoId

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem) = oldItem == newItem
    }) {
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
                imageView.load(photoItem.previewURL) {
                    placeholder(R.drawable.photo_placeholder)
                    listener { _, _ ->
                        shimmerLayoutCell.stopShimmerAnimation()
                    }
                }
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
}

class PixabayViewHolder(val viewBinding: GalleryCellBinding) :
    RecyclerView.ViewHolder(viewBinding.root)

class FooterAdapter(private val retry: () -> Unit) : LoadStateAdapter<FooterViewHolder>() {
    override fun onBindViewHolder(holder: FooterViewHolder, loadState: LoadState) {
        holder.viewBinding.apply {
            progressBar.isVisible = loadState is LoadState.Loading
            holder.itemView.isClickable = loadState is LoadState.Error
            when (loadState) {
                is LoadState.Loading -> textView.text = "正在加载"
                is LoadState.Error -> textView.text = "加载出错，点击重试"
                else -> textView.text = "加载完毕"
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