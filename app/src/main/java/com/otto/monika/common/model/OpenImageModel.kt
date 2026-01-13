package com.otto.monika.common.model

import com.flyjingfish.openimagelib.beans.OpenImageUrl
import com.flyjingfish.openimagelib.enums.MediaType


class OpenImageModel(var photoUrl: String) : OpenImageUrl {

    override fun getImageUrl(): String {
        return photoUrl
    }

    override fun getVideoUrl(): String? {
        return null
    }

    override fun getCoverImageUrl(): String? {
        return null
    }

    override fun getType(): MediaType {
        return MediaType.IMAGE
    }

    companion object {
        fun getImages(images: List<String>): List<OpenImageModel> {
            val openImages = mutableListOf<OpenImageModel>()
            images.forEach {
                val openImageModel = OpenImageModel(it)
                openImages.add(openImageModel)
            }
            return openImages
        }

        fun getImage(image: String): List<OpenImageModel> {
            val openImages = mutableListOf<OpenImageModel>()
            val openImageModel = OpenImageModel(image)
            openImages.add(openImageModel)
            return openImages
        }
    }
}
