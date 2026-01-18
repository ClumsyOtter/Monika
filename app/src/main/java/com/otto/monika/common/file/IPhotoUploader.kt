package com.otto.monika.common.file

import android.content.Intent
import android.net.Uri

interface IPhotoUploader {

    fun selectTakePhoto(photoListener: PhotoListener?, crop: Boolean)

    fun selectGallery(photoListener: PhotoListener?, crop: Boolean)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun startCropPhoto(uri: Uri, photoListener: PhotoListener?)

    interface PhotoListener {
        fun onReceive(filePath: String?, uri: Uri?)
    }

    companion object {
        const val TAKE_PHOTO: Int = 10000 // 拍照
        const val GALLERY: Int = 40000 //图库直接选择
        const val CROP: Int = 20000
        const val PHOTO_RESULT: Int = 30000 // 结果
    }
}
