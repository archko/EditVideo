package com.thuypham.ptithcm.editvideo.ui.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseActivity
import com.thuypham.ptithcm.editvideo.databinding.ActivityMainBinding
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment.Companion.RESULT_PATH
import com.thuypham.ptithcm.editvideo.ui.fragment.result.ResultFragment

class ResultActivity : AppCompatActivity() {


    companion object {
        @JvmStatic
        fun start(context: Context, resultPath: String) {
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra(RESULT_PATH, resultPath)
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

        val resultFragment = ResultFragment()
        resultFragment.arguments = bundleOf(
            RESULT_PATH to intent.getStringExtra(RESULT_PATH)
        )
        supportFragmentManager
            .beginTransaction()
            .add(
                android.R.id.content,
                resultFragment
            ).commit()
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