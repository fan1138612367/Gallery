package com.example.gallery

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

enum class NetworkStatus {
    INITIAL_LOADING,
    LOADING,
    LOADED,
    FAILED,
    COMPLETED
}

class PixabayDataSource(private val context: Context) : PageKeyedDataSource<Int, PhotoItem>() {
    var retry: (() -> Any)? = null  //保存函数
    private val _networkStatus = MutableLiveData<NetworkStatus>()
    val networkStatus: LiveData<NetworkStatus> = _networkStatus
    private val queryKey =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal").random()

    override fun loadInitial(   //实现成员，第一次加载
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, PhotoItem>
    ) {
        retry = null
        _networkStatus.postValue(NetworkStatus.INITIAL_LOADING)
        val url =
            "https://pixabay.com/api/?key=21629751-645aaa88c718127fa1b066aba&q=${queryKey}&per_page=50&page=1"
        StringRequest(
            Request.Method.GET,
            url,
            {
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()   //Gson解析并赋值
                callback.onResult(dataList, null, 2)
                _networkStatus.postValue(NetworkStatus.LOADED)
            },
            {
                retry = { loadInitial(params, callback) }   //保存函数
                _networkStatus.postValue(NetworkStatus.FAILED)
                Log.d("hello", "loadInitial:$it")
            }
        ).also {
            VolleySingleton.getInstance(context).requestQueue.add(it)   //添加到队列中
        }
    }

    override fun loadBefore(    //实现成员，向前加载
        params: LoadParams<Int>,
        callback: LoadCallback<Int, PhotoItem>
    ) {
        
    }

    override fun loadAfter( //实现成员，加载下一页
        params: LoadParams<Int>,
        callback: LoadCallback<Int, PhotoItem>
    ) {
        retry = null
        _networkStatus.postValue(NetworkStatus.LOADING)
        val url =
            "https://pixabay.com/api/?key=21629751-645aaa88c718127fa1b066aba&q=${queryKey}&per_page=50&page=${params.key}"
        StringRequest(
            Request.Method.GET,
            url,
            {
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()   //Gson解析并赋值
                callback.onResult(dataList, params.key + 1)
                _networkStatus.postValue(NetworkStatus.LOADED)
            },
            {
                if (it.toString() == "com.android.volley.ClientError") {
                    _networkStatus.postValue(NetworkStatus.COMPLETED)
                } else {
                    retry = { loadAfter(params, callback) } //保存函数
                    _networkStatus.postValue(NetworkStatus.FAILED)
                }
                Log.d("hello", "loadAfter:$it")
            }
        ).also {
            VolleySingleton.getInstance(context).requestQueue.add(it)
        }
    }
}