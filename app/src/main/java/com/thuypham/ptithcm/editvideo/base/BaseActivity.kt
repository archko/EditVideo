package com.thuypham.ptithcm.editvideo.base

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.thuypham.ptithcm.editvideo.extension.hideKeyBoard
import com.thuypham.ptithcm.editvideo.ui.dialog.ProgressDialog

abstract class BaseActivity<T : ViewDataBinding>(private val layoutId: Int) : AppCompatActivity() {

    lateinit var binding: T
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("thuyyy", "bonCreate")
        binding = DataBindingUtil.setContentView(this, layoutId)
        binding.lifecycleOwner = this
        setupView()
        setupLogic()
        setupDataObserver()

        dialog = ProgressDialog.progressDialog(this)
    }

    open fun setupLogic() {}
    abstract fun setupView()
    open fun setupDataObserver() {}

    override fun finish() {
        binding.root.hideKeyBoard()
        super.finish()
    }


    fun hideLoading() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun showLoading() {
        dialog.show()
    }


//
//    /**
//     * Overrides the pending Activity transition by performing the "Enter" animation.
//     */
//    private fun overridePendingTransitionEnter() {
//        if (isBottomUpActivity()) {
//            overridePendingTransition(R.anim.slide_up, R.anim.stay)
//        } else {
//            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
//        }
//    }
//
//    /**
//     * Overrides the pending Activity transition by performing the "Exit" animation.
//     */
//    private fun overridePendingTransitionExit() {
//        if (isBottomUpActivity()) {
//            overridePendingTransition(R.anim.stay, R.anim.slide_down)
//        } else {
//            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
//        }
//    }

}