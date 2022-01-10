package com.example.gallery

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.example.gallery.databinding.FragmentGalleryBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy { GalleryAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Toast.makeText(requireContext(), getString(R.string.toast_quit), Toast.LENGTH_SHORT)
                .show()
            isEnabled = false
            lifecycleScope.launch {
                delay(1500)
                isEnabled = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {   //重写方法
        super.onViewCreated(view, savedInstanceState)

        val galleryViewModel by activityViewModels<GalleryViewModel>()
        setHasOptionsMenu(true)
        adapter.apply {
            binding.recyclerView.adapter = this.withLoadStateFooter(FooterAdapter { retry() })
            galleryViewModel.pagingData.observe(viewLifecycleOwner) {
                submitData(viewLifecycleOwner.lifecycle, it)
            }
            addLoadStateListener {
                when (it.refresh) {
                    is LoadState.NotLoading -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(800)
                            binding.swipeLayoutGallery.isRefreshing = false
                        }
                    }
                    is LoadState.Loading -> {
                        binding.swipeLayoutGallery.isRefreshing = true
                    }
                    is LoadState.Error -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(3000)
                            binding.swipeLayoutGallery.isRefreshing = false
                            retry()
                        }
                    }
                }
            }
            binding.swipeLayoutGallery.setOnRefreshListener {
                refresh()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {  //重写方法，加载menu
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {   //重写方法，设置menu功能
        when (item.itemId) {
            R.id.swipeIndicator -> adapter.refresh()
        }
        return super.onOptionsItemSelected(item)
    }
}