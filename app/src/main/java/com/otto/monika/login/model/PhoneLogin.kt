package com.otto.monika.login.model

import android.os.Parcelable
import com.otto.monika.login.PhoneLoginActivity
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhoneLogin(
    val source: PhoneLoginActivity.Companion.Source,
    val phoneNumber: String? = null,
    val userId: String? = null
) :
    Parcelable