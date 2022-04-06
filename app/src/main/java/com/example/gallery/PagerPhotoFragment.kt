package com.example.gallery

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.gallery.databinding.FragmentPagerPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PagerPhotoFragment : Fragment() {
    private var _binding: FragmentPagerPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPagerPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {   //重写方法
        super.onViewCreated(view, savedInstanceState)

        val galleryViewModel by activityViewModels<GalleryViewModel>()
        val pagerPhotoAdapter by lazy { PagerPhotoAdapter(requireActivity() as AppCompatActivity) }
        binding.viewPager2.apply {
            adapter = pagerPhotoAdapter
            galleryViewModel.pagingData.observe(viewLifecycleOwner) {
                pagerPhotoAdapter.submitData(viewLifecycleOwner.lifecycle, it)
                setCurrentItem(arguments?.getInt("PHOTO_POSITION") ?: 0, false)
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {    //重写方法，所选页面
                    super.onPageSelected(position)
                    binding.photoTag.text = getString(
                        R.string.photo_tag,
                        position + 1,
                        pagerPhotoAdapter.itemCount
                    )
                }
            })
        }
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->    //动态申请权限
                permissions.entries.forEach {
                    Log.d("Hello", "${it.key} = ${it.value}")
                }
                if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { savePhoto() }   //协程处理
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.save_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        binding.saveButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT > 28    //已获取权限
            ) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { savePhoto() }    //协程处理
            } else {    //未获取权限
                requestPermissionLauncher.launch(   //请求权限
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun savePhoto() {
        val holder =
            (binding.viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(binding.viewPager2.currentItem) as PagerPhotoViewHolder    //大图的Holder
        val bitmap = holder.viewBinding.imageView.drawable.toBitmap()  //转换为位图
        val saveUri = requireContext().contentResolver.insert(  //保存路径占位
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues()
        ) ?: kotlin.run {
            MainScope().launch {
                Toast.makeText(requireContext(), getString(R.string.save_fail), Toast.LENGTH_SHORT)
                    .show()
            }
            return
        }
        requireContext().contentResolver.openOutputStream(saveUri).use {
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) {  //保存图片
                MainScope().launch {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.save_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                MainScope().launch {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.save_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}