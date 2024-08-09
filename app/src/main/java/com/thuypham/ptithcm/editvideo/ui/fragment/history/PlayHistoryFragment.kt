package com.thuypham.ptithcm.editvideo.ui.fragment.history

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import com.archko.editvideo.db.VideoProgress
import com.archko.editvideo.ui.activity.MExoPlayerActivity
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentHistoryBinding
import com.thuypham.ptithcm.editvideo.extension.gone
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.SpacesItemDecoration
import com.thuypham.ptithcm.editvideo.viewmodel.HistoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayHistoryFragment :
    BaseFragment<FragmentHistoryBinding>(R.layout.fragment_history) {

    private val historyViewModel: HistoryViewModel by viewModel()

    private val mediaAdapter: MediaAdapter by lazy {
        MediaAdapter(
            onItemPlay = { onItemPlayClick(it) },
            onItemDeleted = { onItemDeleteClick(it) }
        )
    }

    @OptIn(UnstableApi::class)
    private fun onItemPlayClick(progress: VideoProgress) {
        progress.path?.let {
            val list = listOf(progress.path!!)
            MExoPlayerActivity.start(requireContext(), list, 0)
        }
    }

    private fun onItemDeleteClick(progress: VideoProgress) {
        progress.path?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                historyViewModel.deleteProgress(progress)
            }
        }
    }

    override fun setupLogic() {
        super.setupLogic()
        getData()
    }

    private fun getData() {
    }

    override fun setupView() {
        setupRecyclerView()
        setupToolbar()
        binding.swRefreshLayout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch { loadHistrories() }
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.menu_play_history))
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    private fun goBack() {
        requireActivity().finish()
    }

    private fun setupRecyclerView() {
        binding.apply {
            rvImages.addItemDecoration(SpacesItemDecoration(4))
            rvImages.adapter = mediaAdapter
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadHistrories()
            }
        }
        historyViewModel.deleteResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    viewLifecycleOwner.lifecycleScope.launch { loadHistrories() }
                }

                is ResponseHandler.Loading -> {
                    showLoading()
                }

                is ResponseHandler.Failure -> {
                    hideLoading()
                    showSnackBar("Delete history failed.")
                }

                else -> {
                    hideLoading()
                }
            }
        }
    }

    private suspend fun CoroutineScope.loadHistrories() {
        historyViewModel.loadHistories().collectLatest { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading1()
                    val data = response.data
                    Log.d(
                        this::class.java.name,
                        "PlayHistoryFragment, resultPath: $data"
                    )
                    if (data.isNullOrEmpty()) {
                        binding.layoutEmpty.root.show()
                    } else {
                        binding.layoutEmpty.root.gone()
                        mediaAdapter.submitList(data)
                    }
                }

                is ResponseHandler.Loading -> {
                    if (!binding.swRefreshLayout.isRefreshing) {
                        showLoading()
                    }
                }

                is ResponseHandler.Failure -> {
                    response.extra?.let { showSnackBar(it) }
                    hideLoading1()
                }

                else -> {
                    hideLoading1()
                }
            }
        }
    }

    private fun hideLoading1() {
        hideLoading()
        binding.swRefreshLayout.isRefreshing = false
    }
}