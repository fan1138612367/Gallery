package com.example.gallery

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {   //重写方法
        super.onViewCreated(view, savedInstanceState)

        galleryViewModel = activityViewModels<GalleryViewModel>().value
        val galleryAdapter = GalleryAdapter(galleryViewModel)
        setHasOptionsMenu(true) //设置menu
        binding.recyclerView.apply {
            adapter = galleryAdapter    //设置适配器
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)  //设置两列交错
        }

        galleryViewModel.pagedListLiveData.observe(viewLifecycleOwner, {
            galleryAdapter.submitList(it)   //当数据改变时提交
        })
        binding.swipeLayoutGallery.setOnRefreshListener {   //设置下拉刷新
            galleryViewModel.resetQuery()   //重新获取数据
        }
        galleryViewModel.networkStatus.observe(viewLifecycleOwner, {
            Log.d("hello", "onViewCreated:$it")
            galleryAdapter.updateNetworkStatus(it)
            binding.swipeLayoutGallery.isRefreshing =
                it == NetworkStatus.INITIAL_LOADING  //只有初次加载为true
        })
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
                    { galleryViewModel.resetQuery() },
                    500
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