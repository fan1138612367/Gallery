package com.example.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
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

class GalleryAdapter(private val galleryViewModel: GalleryViewModel) :
    PagedListAdapter<PhotoItem, RecyclerView.ViewHolder>(DiffCallback) {
    private var hasFooter = false
    private var networkStatus: NetworkStatus? = null

    init {
        galleryViewModel.retry()
    }

    override fun getItemCount(): Int {  //重写方法
        return super.getItemCount() + if (hasFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasFooter && position == itemCount - 1) R.layout.gallery_footer else R.layout.gallery_cell
    }

    fun updateNetworkStatus(networkStatus: NetworkStatus?) {
        this.networkStatus = networkStatus
        if (networkStatus == NetworkStatus.INITIAL_LOADING) hideFooter() else showFooter()
    }

    private fun hideFooter() {
        if (hasFooter) {
            notifyItemRemoved(itemCount - 1)
        }
        hasFooter = false
    }

    private fun showFooter() {
        if (hasFooter) {
            notifyItemChanged(itemCount - 1)
        } else {
            hasFooter = true
            notifyItemInserted(itemCount - 1)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {   //实现成员，加载View
        return when (viewType) {
            R.layout.gallery_cell -> PhotoViewHolder.newInstance(parent).also { holder ->
                holder.itemView.setOnClickListener {
                    Bundle().apply {
                        putInt("PHOTO_POSITION", holder.adapterPosition)    //传递当前图片位置
                        holder.itemView.findNavController()
                            .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)
                    }
                }
            }
            else -> FooterViewHolder.newInstance(parent).also {
                it.itemView.setOnClickListener {
                    galleryViewModel.retry()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {    //实现成员，加载图片
        when (holder.itemViewType) {
            R.layout.gallery_footer -> (holder as FooterViewHolder).bindWithNetworkStatus(
                networkStatus
            )
            else -> (holder as PhotoViewHolder).bindWithPhotoItem(getItem(position) ?: return)
        }
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

class PhotoViewHolder(private val viewBinding: GalleryCellBinding) :
    RecyclerView.ViewHolder(viewBinding.root) { //自定义ViewHolder
    companion object {
        fun newInstance(parent: ViewGroup): PhotoViewHolder {
            val binding =
                GalleryCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PhotoViewHolder(binding)
        }
    }

    fun bindWithPhotoItem(photoItem: PhotoItem) {
        with(viewBinding) {
            shimmerLayoutCell.apply {   //设置闪动
                setShimmerColor(0x55FFFFFF) //闪动颜色
                setShimmerAngle(0)  //闪动角度
                startShimmerAnimation() //开始闪动
            }
            textViewUser.text = photoItem.photoUser
            textViewLikes.text = photoItem.photoLikes.toString()
            textViewFavorites.text = photoItem.photoFavorites.toString()
            imageView.layoutParams.height = photoItem.photoHeight   //设置图片高度
        }
        Glide.with(itemView) //Glide加载图片
            .load(photoItem.previewURL) //加载预览图
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
                    return false.also { viewBinding.shimmerLayoutCell.stopShimmerAnimation() }   //停止闪动
                }
            })
            .into(viewBinding.imageView)
    }
}

class FooterViewHolder(private val viewBinding: GalleryFooterBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    companion object {
        fun newInstance(parent: ViewGroup): FooterViewHolder {
            val binding =
                GalleryFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            (binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
            return FooterViewHolder(binding)
        }
    }

    fun bindWithNetworkStatus(networkStatus: NetworkStatus?) {
        with(viewBinding) {
            when (networkStatus) {
                NetworkStatus.FAILED -> {
                    textView.text = "点击重试"
                    progressBar.visibility = View.GONE
                    itemView.isClickable = true
                }
                NetworkStatus.COMPLETED -> {
                    textView.text = "加载完毕"
                    progressBar.visibility = View.GONE
                    itemView.isClickable = false
                }
                else -> {
                    textView.text = "正在加载"
                    progressBar.visibility = View.VISIBLE
                    itemView.isClickable = false
                }
            }
        }
    }
}