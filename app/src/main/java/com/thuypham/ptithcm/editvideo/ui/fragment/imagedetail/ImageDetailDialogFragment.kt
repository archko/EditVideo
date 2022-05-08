package com.thuypham.ptithcm.editvideo.ui.fragment.imagedetail

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import coil.load
import coil.request.CachePolicy
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentDetailImageBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.widget.ZoomImageView

class ImageDetailDialogFragment(
    private val imagePath: String,
) : BaseDialogFragment<FragmentDetailImageBinding>(R.layout.fragment_detail_image),
    ZoomImageView.ZoomImageListener {

    override val isFullScreen = true
    private var isShowController = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setStyle(STYLE_NO_FRAME, 0)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }


    override fun setupView() {
        binding.apply {
            if (!imagePath.isNullOrEmpty()) {
                if (imagePath.startsWith("/storage")) {
                    ivZoomImage.load(imagePath) {
                        crossfade(true)
                        diskCachePolicy(CachePolicy.DISABLED)
                        placeholder(R.drawable.ic_image_placeholder)
                    }
                } else {
                    ivZoomImage.load(imagePath) {
                        crossfade(true)
                        placeholder(R.drawable.ic_image_placeholder)
                    }
                }
            }

            ivZoomImage.setImageListener(this@ImageDetailDialogFragment)
            ivClose.setOnSingleClickListener {
                dismiss()
            }
            toggleControllerVisibility()
        }
    }

    override fun onImageClick() {
        isShowController = !isShowController
        toggleControllerVisibility()
    }

    private fun toggleControllerVisibility() {
        binding.flTopControl.isVisible = isShowController
        if (isShowController) {
            binding.flTopControl.postDelayed({
                isShowController = !isShowController
                toggleControllerVisibility()
            }, 3000)
        }
    }

    override fun onLongClick() {

    }
}