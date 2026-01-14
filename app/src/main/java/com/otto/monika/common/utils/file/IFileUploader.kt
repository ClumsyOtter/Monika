package com.otto.monika.common.utils.file

import android.net.Uri

interface IFileUploader {
    fun uploadFile(filePath: String?, updateListener: UpdateListener<ResultUrl?>?)
    fun uploadFileUri(uri: Uri?, updateListener: UpdateListener<ResultUrl?>?)
}
