package com.example.gallery

import androidx.lifecycle.*
import androidx.paging.*

class GalleryViewModel : ViewModel() {
    val pagingData = PixabayRepository.getPagingData().cachedIn(viewModelScope).asLiveData()
}