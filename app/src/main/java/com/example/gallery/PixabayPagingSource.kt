package com.example.gallery

import androidx.paging.PagingSource
import androidx.paging.PagingState

class PixabayPagingSource(private val pixabayService: PixabayService) :
    PagingSource<Int, PhotoItem>() {
    private val queryKey = arrayOf(
        "cat",
        "dog",
        "car",
        "beauty",
        "phone",
        "computer",
        "flower",
        "animal"
    ).random()

    override fun getRefreshKey(state: PagingState<Int, PhotoItem>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PhotoItem> {
        return try {
            val page = params.key ?: 1
            val pixabay = pixabayService.searchPhoto(queryKey, page).hits
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (pixabay.size == 50) page + 1 else null
            LoadResult.Page(pixabay, prevKey, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}