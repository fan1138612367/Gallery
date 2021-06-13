package com.example.gallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

class GalleryViewModel(application: Application) :
    AndroidViewModel(application) {  //AndroidViewModel需要添加参数
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive: LiveData<List<PhotoItem>> = _photoListLive

    fun fetchData() {   //获取数据
        val stringRequest = StringRequest(
            Request.Method.GET,
            getURL(),
            {
                _photoListLive.value =  //正确响应
                    Gson().fromJson(it, Pixabay::class.java).hits.toList()   //Gson解析并赋值
            },
            {
                Log.d("hello", it.toString())   //错误响应
            }
        )
        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)   //添加到队列中
    }

    private fun getURL(): String {
        return "https://pixabay.com/api/?key=21629751-645aaa88c718127fa1b066aba&q=${keyWords.random()}&per_page=100"
    }

    private val keyWords =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal")
}