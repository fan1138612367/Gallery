package com.example.gallery

import androidx.paging.PagingSource
import androidx.paging.PagingState

class PixabayPagingSource(private val pixabayService: PixabayService) :
    PagingSource<Int, PhotoItem>() {
    override fun getRefreshKey(state: PagingState<Int, PhotoItem>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PhotoItem> {
        return try {
            val page = params.key ?: 1
            val pixabay = pixabayService.searchPhoto(
                arrayOf(
                    "cat",
                    "dog",
                    "car",
                    "beauty",
                    "phone",
                    "computer",
                    "flower",
                    "animal"
                ).random()
            )
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (pixabay.hits.isNotEmpty()) page + 1 else null
            LoadResult.Page(pixabay.hits, prevKey, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}