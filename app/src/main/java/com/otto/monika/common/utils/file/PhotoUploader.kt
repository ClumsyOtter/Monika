package com.otto.monika.common.utils.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.otto.monika.common.permission.PermissionDialog
import com.otto.monika.common.permission.SimplePermResult
import com.otto.monika.common.utils.FileUri
import com.yalantis.ucrop.UCrop
import java.io.File


class PhotoUploader : IPhotoUploader {
    private val mActivity: Activity
    private var mPhotoUri: Uri? = null
    private var mPhotoListener: IPhotoUploader.PhotoListener? = null
    private var isCrop = false
    private var aspectX = 1.0f
    private var aspectY = 1.0f
    private var isPlaceCrop = false

    constructor(activity: Activity) : super() {
        mActivity = activity
        PermissionDialog.request(
            activity,
            "请先同意相机权限和存储权限, 才能使用该功能.",
            "相机权限, 存储权限",
            mutableListOf<String>(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    constructor(
        activity: Activity,
        aspectX: Int,
        aspectY: Int,
    ) : super() {
        mActivity = activity
        this.aspectX = aspectX.toFloat()
        this.aspectY = aspectY.toFloat()
        PermissionDialog.requestCamera(activity)
    }

    constructor(
        activity: Activity,
        aspectX: Int,
        aspectY: Int,
        isPlaceCrop: Boolean
    ) : super() {
        mActivity = activity
        this.aspectX = aspectX.toFloat()
        this.aspectY = aspectY.toFloat()
        this.isPlaceCrop = isPlaceCrop
        PermissionDialog.requestCamera(activity)
    }

    override fun selectTakePhoto(photoListener: IPhotoUploader.PhotoListener?, crop: Boolean) {
        PermissionDialog.requestStorage(mActivity, object : SimplePermResult() {
            override fun granted() {
                isCrop = crop
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                var mTmpFile: File? = null
                try {
                    mTmpFile = createTmpFile(mActivity)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                if (mTmpFile != null && mTmpFile.exists()) {
                    mPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                        mActivity,
                        mActivity.packageName + ".fileprovider", mTmpFile
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri)
                    mActivity.startActivityForResult(
                        intent,
                        IPhotoUploader.TAKE_PHOTO
                    )
                    mPhotoListener = photoListener
                }
            }
        })
    }

    fun createTmpFile(context: Context): File? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            return File.createTempFile(
                "IMG_",
                ".jpg",
                dir
            )
        } catch (ignored: java.lang.Exception) {
        }
        return null
    }

    override fun selectGallery(photoListener: IPhotoUploader.PhotoListener?, crop: Boolean) {
        PermissionDialog.requestStorage(mActivity, object : SimplePermResult() {
            override fun granted() {
                mPhotoListener = photoListener
                isCrop = crop
                val resultCode = if (isCrop) {
                    IPhotoUploader.CROP
                } else {
                    IPhotoUploader.GALLERY
                }
                runCatching {
                    val intent = (Intent("android.intent.action.GET_CONTENT")).setType("image/*")
                    mActivity.startActivityForResult(intent, resultCode)
                }
            }
        })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {
            IPhotoUploader.CROP -> startCropPhoto(data?.data)
            IPhotoUploader.PHOTO_RESULT -> if (resultCode == Activity.RESULT_OK) {
                val output: Uri? = data?.let { UCrop.getOutput(it) }
                setResult(output)
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Toast.makeText(
                    mActivity,
                    data?.let { UCrop.getError(it) }?.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            IPhotoUploader.GALLERY -> setResult(data?.data)
            IPhotoUploader.TAKE_PHOTO -> gotTheImage(mPhotoUri)
            UCrop.REQUEST_CROP -> if (resultCode == Activity.RESULT_OK) {
                val resultUri: Uri? = data?.let { UCrop.getOutput(it) }
                setResult(resultUri)
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Toast.makeText(
                    mActivity,
                    data?.let { UCrop.getError(it) }?.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            UCrop.RESULT_ERROR -> {}
        }
    }

    private fun gotTheImage(uri: Uri?) {
        if (uri == null) return
        if (isCrop) {
            startCropPhoto(uri)
        } else {
            setResult(uri)
        }
    }

    private fun setResult(uri: Uri?) {
        try {
            mPhotoListener?.onReceive(FileUri.getFileAbsolutePath(mActivity, uri), uri)
        } catch (e: Exception) {
        }
    }

    /**
     * 图片缩放
     *
     * @param uri
     */
    override fun startCropPhoto(uri: Uri, photoListener: IPhotoUploader.PhotoListener?) {
        mPhotoListener = photoListener
        startCropPhoto(uri)
    }

    private fun startCropPhoto(uri: Uri?) {
        if (uri == null) return
        if (isPlaceCrop) {
            //裁剪后保存到文件中
            checkDir()
            val destinationUri = Uri.fromFile(
                File(
                    mActivity.externalCacheDir?.absolutePath + "/SuperJK",
                    "SampleCropImage.jpeg"
                )
            )
            val options: UCrop.Options = UCrop.Options()
            options.setHideBottomControls(true)
            UCrop.of(uri, destinationUri)
                .withAspectRatio(2.08f, 1f)
                .withOptions(options)
                .withMaxResultSize(800, 800)
                .start(mActivity)
            return
        }
        val destination = Uri.fromFile(
            File(
                mActivity.cacheDir,
                System.currentTimeMillis().toString() + ""
            )
        )
        UCrop.of(uri, destination).withAspectRatio(aspectX, aspectY)
            .start(mActivity, IPhotoUploader.PHOTO_RESULT)
    }

    private fun checkDir() {
        val file = File(mActivity.externalCacheDir?.absolutePath + "/SuperJK")
        if (!file.exists()) {
            file.mkdirs()
        }
    }
}
