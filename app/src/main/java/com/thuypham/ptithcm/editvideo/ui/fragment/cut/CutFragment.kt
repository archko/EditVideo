package com.thuypham.ptithcm.editvideo.ui.fragment.cut

import android.util.Log
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentCutBinding
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.viewmodel.CutViewModel
import com.thuypham.ptithcm.editvideo.viewmodel.ExtractImageViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class CutFragment : BaseFragment<FragmentCutBinding>(R.layout.fragment_cut) {

    private val cutViewModel: CutViewModel by viewModel()
    private val extractImageViewModel: ExtractImageViewModel by viewModel()

    private var folderPath: String? = null

    override fun setupLogic() {
        super.setupLogic()
        folderPath = arguments?.getString(HomeFragment.RESULT_PATH)
        getData()
    }

    private fun getData() {
        extractImageViewModel.getImageExtracted(folderPath)
    }

    override fun setupView() {
        setupToolbar()
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.title_cut_video))
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
        setSubRightBtn(R.drawable.ic_delete) {
            extractImageViewModel.deleteImage(folderPath)
            null
        }
    }

    private fun goBack() {
        requireActivity().finish()
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        cutViewModel.cutResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    requireActivity().finish()
                }
                is ResponseHandler.Loading -> {
                }
                is ResponseHandler.Failure -> {
                }
                else -> {
                }
            }
        }
        extractImageViewModel.imagesResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading1()
                    val data = response.data
                    Log.d(
                        this::class.java.name,
                        "CutFragment, resultPath:$folderPath, $data"
                    )
                    if (data.isEmpty()) {
                    } else {
                    }
                }
                is ResponseHandler.Loading -> {
                    //if (!binding.swRefreshLayout.isRefreshing) {
                    showLoading()
                    //}
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
        extractImageViewModel.deleteImagesResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    goBack()
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    hideLoading()
                    showSnackBar("Delete image failed.")
                }
                else -> {
                    hideLoading()
                }
            }
        }
    }

    private fun hideLoading1() {
        hideLoading()
    }
}

