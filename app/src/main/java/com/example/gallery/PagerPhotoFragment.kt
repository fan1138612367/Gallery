package com.example.gallery

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.gallery.databinding.FragmentPagerPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PagerPhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PagerPhotoFragment : Fragment() {
    private lateinit var binding: FragmentPagerPhotoBinding
    private lateinit var galleryViewModel: GalleryViewModel

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPagerPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {   //重写方法
        super.onViewCreated(view, savedInstanceState)

        galleryViewModel = activityViewModels<GalleryViewModel>().value
        val adapter = PagerPhotoListAdapter()
        binding.viewPager2.adapter = adapter
        galleryViewModel.pagedListLiveData.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            binding.viewPager2.setCurrentItem(
                arguments?.getInt("PHOTO_POSITION") ?: 0,
                false
            )  //设置当前页
        })

        binding.viewPager2.apply {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {    //重写方法，所选页面
                    super.onPageSelected(position)
                    binding.photoTag.text = getString(
                        R.string.photo_tag,
                        position + 1,
                        galleryViewModel.pagedListLiveData.value?.size
                    )
                }
            })
            setCurrentItem(arguments?.getInt("PHOTO_POSITION") ?: 0, false) //设置当前页面
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->    //动态申请权限
                permissions.entries.forEach {
                    Log.d("hello", "${it.key} = ${it.value}")
                }
                if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    viewLifecycleOwner.lifecycleScope.launch { savePhoto() }   //协程处理
                } else {
                    Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                }
            }
        binding.saveButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT > 28 -> {   //已获取权限
                    viewLifecycleOwner.lifecycleScope.launch { savePhoto() }    //协程处理
                }
                else -> {   //未获取权限
                    requestPermissionLauncher.launch(   //请求权限
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    )
                }
            }
        }
    }

    private suspend fun savePhoto() {   //允许挂起
        withContext(Dispatchers.IO) {
            val holder =
                (binding.viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(binding.viewPager2.currentItem) as PagerPhotoViewHolder    //大图的Holder
            val bitmap = holder.viewBinding.pagerPhoto.drawable.toBitmap()  //转换为位图
            val saveUri = requireContext().contentResolver.insert(  //保存路径占位
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            ) ?: kotlin.run {
                MainScope().launch {
                    Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            requireContext().contentResolver.openOutputStream(saveUri).use {
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) {  //保存图片
                    MainScope().launch {
                        Toast.makeText(requireContext(), "存储成功", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    MainScope().launch {
                        Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PagerPhotoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PagerPhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}