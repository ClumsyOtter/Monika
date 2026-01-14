package com.otto.monika.common.utils.file

import android.content.Intent
import android.net.Uri

interface IPhotoUploader : IFileUploader {
    fun setPhotoDisposer(photoDisposer: IPhotoDisposer?)

    fun selectTakePhoto(photoListener: PhotoListener?, crop: Boolean)

    fun selectGallery(photoListener: PhotoListener?, crop: Boolean)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent)

    fun startCropPhoto(uri: Uri, photoListener: PhotoListener?)

    interface PhotoListener {
        /**
         * tianzhen 待优化
         * 临时方案增加参数,后期有时间可以把 filePath 与 uri 合并
         */
        fun onReceive(filePath: String?, uri: Uri?)
    }

    companion object {
        const val TAKE_PHOTO: Int = 10000 // 拍照
        const val GALLERY: Int = 40000 //图库直接选择
        const val CROP: Int = 20000
        const val PHOTO_RESULT: Int = 30000 // 结果
    }
}
