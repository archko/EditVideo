package com.thuypham.ptithcm.editvideo.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseActivity
import com.thuypham.ptithcm.editvideo.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    var view: View? = null

    override fun setupView() {
        view = findViewById(R.id.container)
        checkSdcardPermission()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                runOnUiThread {
                    if (view != null) {
                        /*val snackBar = Snackbar.make(
                            view ?: return@Runnable,
                            getString(R.string.external_permission_denied), Snackbar.LENGTH_SHORT
                        )
                        snackBar.show()*/
                        showDialog(
                            this,
                            "\"本应用需要获取\"访问所有文件\"权限，请给予此权限，否则无法使用本应用\""
                        ) { _: DialogInterface?, _: Int ->
                            requestSdcardPermission()
                        }
                    }
                }
            }
        }

    private fun getVersion(): Int {
        return try {
            Build.VERSION.SDK.toInt()
        } catch (th: Throwable) {
            3
        }
    }

    private fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Log.i("ABCD", "此手机是Android 11或更高的版本，且已获得访问所有文件权限")
                return true
            } else {
                Log.i("ABCD", "此手机是Android 11或更高的版本，且没有访问所有文件权限")
            }
            false
        } else {
            Log.i(
                "ABCD",
                "此手机版本小于Android 11，版本为：API \${Build.VERSION.SDK_INT}，不需要申请文件管理权限"
            )
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) === PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkSdcardPermission() {
        if (!hasStoragePermission(this)) {
            requestSdcardPermission()
        } else {
        }
    }

    private fun requestSdcardPermission() {
        if (Build.VERSION_CODES.R <= getVersion()) {
            requestPermissionLauncher.launch(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                val permission = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                    Manifest.permission.READ_EXTERNAL_STORAGE
                else
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showDialog(
        activity: Activity,
        message: String,
        clickListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(activity)
            .setTitle("提示")
            .setMessage(message)
            .setPositiveButton(
                "确定"
            ) { dialog: DialogInterface?, which: Int ->
                clickListener.onClick(
                    dialog,
                    which
                )
            }
            .show()
    }
}