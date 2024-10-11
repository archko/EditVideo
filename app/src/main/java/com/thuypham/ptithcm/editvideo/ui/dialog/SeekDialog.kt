package com.thuypham.ptithcm.editvideo.ui.dialog

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.DialogSeekBinding
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth
import kotlin.math.roundToInt

class SeekDialog(
    private val title: String? = null,
    private val okMsg: String? = null,
    private val start: Float = 0.0f,
    private val cancelMsg: String? = null,
    private val okTextColor: Int? = null,
    private val isShowCancelMsg: Boolean? = false,
    private val onConfirmClick: ((Float) -> Unit)? = null,
    private val onCancelClick: (() -> Unit)? = null,
) : BaseDialogFragment<DialogSeekBinding>(R.layout.dialog_seek) {
    companion object {
        const val TAG = "SEEK_DIALOG_TAG"
    }

    override fun setupView() {
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
        binding.apply {
            container.layoutParams.width = (getScreenWidth() * 0.8).roundToInt()
            btnCancel.setOnClickListener {
                onCancelClick?.invoke()
                dismiss()
            }
            btnConfirm.setOnClickListener {
                dismiss()
                val progress = slider.value * 1000
                onConfirmClick?.invoke(progress)
            }

            slider.value = start
            var from = start - 50
            if (from < 0) {
                from = 0f
            }
            slider.valueFrom = from
            var to = start + 200
            slider.valueTo = to
            slider.setLabelFormatter { value ->
                "${value.toInt()}S"
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