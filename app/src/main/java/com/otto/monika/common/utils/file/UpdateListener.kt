package com.otto.monika.common.utils.file

interface UpdateListener<T> {
    fun finishUpdate(result: T?)

    fun onError(error: Error?)
}


