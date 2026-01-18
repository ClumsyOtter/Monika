package com.otto.monika.common.ext

import android.text.InputFilter
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import com.otto.monika.R
import com.otto.monika.common.views.MonikaCustomButton

fun EditText.addInputFilter(inputFilter: InputFilter) {
    val newFilters = arrayOfNulls<InputFilter>(filters.size + 1)
    // 复制现有过滤器
    System.arraycopy(filters, 0, newFilters, 0, filters.size)
    // 添加新的过滤器
    newFilters[filters.size] = inputFilter
    filters = newFilters
}

/**
 * 移除特定类型的 InputFilter
 * @param filterClass 要移除的过滤器类
 */
fun EditText.removeInputFilter(filterClass: Class<*>) {
    val currentFilters = this.filters
    val newFilters = currentFilters.filterNot {
        filterClass.isInstance(it)
    }.toTypedArray()
    this.filters = newFilters
}

fun MonikaCustomButton.enableButton() {
    isClickable = true
    setBackgroundImage(
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.monika_custom_btn_empty_black,
            null
        )
    )
    setTextColor(ResourcesCompat.getColor(resources, R.color.color_C4FF05, null))
}

fun MonikaCustomButton.disableButton(@ColorRes colorId: Int? = null) {
    isClickable = false
    setBackgroundImage(
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.monika_custom_btn_empty_gray,
            null
        )
    )
    colorId?.let {
        setBackgroundImageTint(
            ResourcesCompat.getColor(
                resources,
                colorId,
                null
            )
        )
    }
    setTextColor(
        ResourcesCompat.getColor(
            resources,
            R.color.text_999999,
            null
        )
    )
}