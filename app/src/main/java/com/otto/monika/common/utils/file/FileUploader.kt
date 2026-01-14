package com.otto.monika.common.utils.file

import android.net.Uri

open class FileUploader() : IFileUploader {
    override fun uploadFile(filePath: String?, updateListener: UpdateListener<ResultUrl?>?) {
    }

    override fun uploadFileUri(uri: Uri?, updateListener: UpdateListener<ResultUrl?>?) {
    }
}
