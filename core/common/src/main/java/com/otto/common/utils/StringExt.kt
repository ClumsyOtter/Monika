package com.otto.common.utils

fun String.isValidatePhoneNumber(): Boolean {
    val regex = "^1[3-9]\\d{9}$"
    return this.matches(Regex(regex))
}