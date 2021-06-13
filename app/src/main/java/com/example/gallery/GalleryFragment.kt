package com.example.gallery

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallery.databinding.FragmentGalleryBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryFragment : Fragment() {
    private lateinit var binding: FragmentGalleryBinding
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

        binding = FragmentGalleryBinding.inflate(layoutInflater)
        val galleryAdapter = GalleryAdapter()

        setHasOptionsMenu(true) //设置menu
        binding.recyclerView.apply {
            adapter = galleryAdapter    //设置适配器
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)  //设置两列交错
        }

        galleryViewModel = viewModels<GalleryViewModel>().value
        galleryViewModel.photoListLive.observe(this, {
            galleryAdapter.submitList(it)   //当数据改变时提交
            binding.swipeLayoutGallery.isRefreshing = false //停止刷新
        })
        galleryViewModel.photoListLive.value ?: galleryViewModel.fetchData()    //第一次获取数据
        binding.swipeLayoutGallery.setOnRefreshListener {   //设置下拉刷新
            galleryViewModel.fetchData()    //重新获取数据
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {  //重写方法，加载menu
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {   //重写方法，设置menu功能
        when (item.itemId) {
            R.id.swipeIndicator -> {
                binding.swipeLayoutGallery.isRefreshing = true
                Handler(Looper.getMainLooper()).postDelayed(     //延迟1秒再获取数据
                    { galleryViewModel.fetchData() },
                    1000
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GalleryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GalleryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}