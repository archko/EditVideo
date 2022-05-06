package com.thuypham.ptithcm.editvideo.ui.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseActivity
import com.thuypham.ptithcm.editvideo.databinding.ActivityMainBinding
import com.thuypham.ptithcm.editvideo.ui.fragment.cut.CutFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.extractimage.ExtractImageResultFragment
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment.Companion.RESULT_DESTINATION_ID
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment.Companion.RESULT_PATH
import com.thuypham.ptithcm.editvideo.ui.fragment.result.ResultFragment

class ResultActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_result) {

    companion object {
        @JvmStatic
        fun start(context: Context, resultPath: String, destinationId: Int) {
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra(RESULT_PATH, resultPath)
            intent.putExtra(RESULT_DESTINATION_ID, destinationId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent == null || TextUtils.isEmpty(intent.getStringExtra(RESULT_PATH))) {
            finish()
            return
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val destinationId = intent.getIntExtra(RESULT_DESTINATION_ID, 0)
        val bundle = bundleOf(
            RESULT_PATH to intent.getStringExtra(RESULT_PATH)
        )
        if (destinationId == R.id.home_to_extractImages) {
            val resultFragment = ExtractImageResultFragment()
            resultFragment.arguments = bundle
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.container,
                    resultFragment
                ).commit()
        } else if (destinationId == R.id.home_to_Cut) {
            val resultFragment = CutFragment()
            resultFragment.arguments = bundle
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.container,
                    resultFragment
                ).commit()
        } else {
            val resultFragment = ResultFragment()
            resultFragment.arguments = bundle
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.container,
                    resultFragment
                ).commit()
        }
    }

    override fun setupView() {
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE
        }
    }
}