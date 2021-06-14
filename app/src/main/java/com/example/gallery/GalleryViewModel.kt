package com.example.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData

class GalleryViewModel(application: Application) :
    AndroidViewModel(application) {  //AndroidViewModel需要添加参数
    private val factory = PixabayDataSourceFactory(application)
    val pagedListLiveData = factory.toLiveData(1)
    val networkStatus: LiveData<NetworkStatus> =
        Transformations.switchMap(factory.pixabayDataSource) { it.networkStatus }

    fun resetQuery() {  //作废DataSource
        pagedListLiveData.value?.dataSource?.invalidate()
    }

    fun retry() {
        factory.pixabayDataSource.value?.retry?.invoke()
    }
}