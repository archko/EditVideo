package com.thuypham.ptithcm.editvideo.ui.dialog

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.DialogUrlBinding
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth
import kotlin.math.roundToInt

class OpenUrlDialog(
    private val title: String? = null,
    private val okMsg: String? = null,
    private val cancelMsg: String? = null,
    private val okTextColor: Int? = null,
    private val isShowCancelMsg: Boolean? = false,
    private val onConfirmClick: ((String) -> Unit)? = null,
    private val onCancelClick: (() -> Unit)? = null,
) : BaseDialogFragment<DialogUrlBinding>(R.layout.dialog_url) {
    companion object {
        const val TAG = "URL_DIALOG_TAG"
    }

    override fun setupView() {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        binding.apply {
            container.layoutParams.width = (getScreenWidth() * 0.8).roundToInt()
            btnCancel.setOnClickListener {
                onCancelClick?.invoke()
                dismiss()
            }
            btnConfirm.setOnClickListener {
                dismiss()
                val url = tvUrl.editableText.toString()
                onConfirmClick?.invoke(url)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.apply {
            title?.let {
                tvDialogTitle.text = it
            }

            okMsg?.let {
                btnConfirm.text = it
            }

            btnCancel.isVisible = isShowCancelMsg ?: false
            viewCenterButton.isVisible = isShowCancelMsg ?: false
            cancelMsg?.let {
                btnCancel.text = it
            }

            okTextColor?.let {
                btnConfirm.isAllCaps = false
                btnCancel.isAllCaps = false
                btnConfirm.setTextColor(ContextCompat.getColor(requireContext(), it))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }
}