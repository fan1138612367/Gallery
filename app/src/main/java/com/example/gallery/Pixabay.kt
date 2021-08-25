package com.example.gallery

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pixabay(
    val total: Int, //总数
    val totalHits: Int, //返回数量
    val hits: List<PhotoItem>, //内容
) : Parcelable

@Parcelize
data class PhotoItem(
    @SerializedName("id") val photoId: Int, //ID
    @SerializedName("webformatURL") val previewURL: String, //预览图
    @SerializedName("webformatHeight") val photoHeight: Int,    //预览图高度
    @SerializedName("largeImageURL") val fullURL: String,   //大图
    @SerializedName("favorites") val photoFavorites: Int,    //喜爱数
    @SerializedName("likes") val photoLikes: Int,   //点赞数
    @SerializedName("user") val photoUser: String  //作者
) : Parcelable