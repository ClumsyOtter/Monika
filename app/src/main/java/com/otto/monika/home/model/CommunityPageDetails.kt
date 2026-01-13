package com.otto.monika.home.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize


@Keep
data class CommunityPageDetails(
    val is_more: Int,
    val list: List<CommunityPageDetailItem>?,
    val page: Int
)

@Keep
@Parcelize
data class CommunityPageDetailItem(
    val id: Int?,
    val title: String?,
    val category_id: Int?,
    val channel: String?,
    val content: String?,
    val type: String?,  // 1图文 2视频
    val image: String?,
    val pv: Int?,
    val video_data: VideoData?,
    val created_at: String?,
    val author_name: String?,
    val author_avatar: String?,
    val author_intro: String?,
    val detail_url: String?
) : Parcelable

@Keep
@Parcelize
data class VideoData(
    val cover_img: String?,
    val video_url: String?,
    val video_url_m3u8: String?
) : Parcelable