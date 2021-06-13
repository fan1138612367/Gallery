package com.example.gallery

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(context: Context) { //单个Volley队列
    companion object {
        private var INSTANCE: VolleySingleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {    //线程安全处理
                VolleySingleton(context).also { INSTANCE = it } //创建实例并赋值
            }
    }

    val requestQueue: RequestQueue by lazy {    //成员，VolleySingleton.getInstance().requestQueue获取
        Volley.newRequestQueue(context.applicationContext)
    }
}