package com.example.gallery

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

object PixabayRepository {
    private val pixabayService = PixabayService.create()

    fun getPagingData(): Flow<PagingData<PhotoItem>> {
        return Pager(
            PagingConfig(50)
        ) {
            PixabayPagingSource(pixabayService)
        }.flow
    }
}