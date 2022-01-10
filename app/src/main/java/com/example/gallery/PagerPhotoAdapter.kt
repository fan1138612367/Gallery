package com.example.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.gallery.databinding.PagerPhotoViewBinding

class PagerPhotoAdapter : PagingDataAdapter<PhotoItem, PagerPhotoViewHolder>(
    object : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean { //实现成员
            return oldItem.photoId == newItem.photoId   //判断Item是否相同
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {  //实现成员
            return oldItem == newItem   //判断内容是否相同
        }
    }) {
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
        holder.viewBinding.apply {
            shimmerLayout.apply {
                setShimmerColor(0x55FFFFFF)
                setShimmerAngle(30)
                startShimmerAnimation()
            }
            pagerPhoto.load(getItem(position)?.fullURL) {
                placeholder(R.drawable.photo_placeholder)
                listener { _, _ ->
                    shimmerLayout.stopShimmerAnimation()
                }
            }
        }
    }
}

class PagerPhotoViewHolder(val viewBinding: PagerPhotoViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root)