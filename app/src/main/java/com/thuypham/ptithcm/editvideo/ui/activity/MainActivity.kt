package com.thuypham.ptithcm.editvideo.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseActivity
import com.thuypham.ptithcm.editvideo.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    var view: View? = null

    override fun setupView() {
        view = findViewById(R.id.container)
        if (isStoragePermissionGranted()) {

        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                runOnUiThread(Runnable {
                    if (view != null) {
                        val snackBar = Snackbar.make(
                            view ?: return@Runnable, getString(R.string.external_permission_denied)
                                ?: return@Runnable, Snackbar.LENGTH_SHORT
                        )
                        snackBar.show()
                    }
                })
            }
        }

    private fun isStoragePermissionGranted(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permission = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                Manifest.permission.READ_EXTERNAL_STORAGE
            else
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            requestPermissionLauncher.launch(permission)
            return false
        }
        return true
    }
}