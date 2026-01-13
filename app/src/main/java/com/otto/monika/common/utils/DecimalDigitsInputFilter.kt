package com.otto.monika.common.utils

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils

/**
 * 只限制小数点位数的InputFilter
 * 功能单一，只负责限制小数点后的位数
 * @param maxDecimalDigits 小数部分最大位数
 */
class DecimalDigitsInputFilter(
    private val maxDecimalDigits: Int = 1
) : InputFilter {

    init {
        // 确保小数位数为非负数
        require(maxDecimalDigits >= 0) { "maxDecimalDigits must be non-negative" }
    }

    override fun filter(
        source: CharSequence?,  // 用户输入的新内容
        start: Int,             // 新内容的开始位置
        end: Int,               // 新内容的结束位置
        dest: Spanned?,         // 原始文本
        dstart: Int,            // 新内容在原始文本中的插入位置
        dend: Int               // 新内容替换原始文本的结束位置
    ): CharSequence? {
        // 如果是删除操作，允许
        if (TextUtils.isEmpty(source)) {
            return null
        }
        val originalText = dest?.toString() ?: ""
        val sourceText = source?.toString() ?: ""
        // 构建新文本
        val newText = StringBuilder(originalText)
            .replace(dstart, dend, sourceText)
            .toString()

        // 如果没有小数点，不需要限制
        if (!newText.contains('.')) {
            return null
        }
        // 检查小数部分是否超过限制
        val dotIndex = newText.indexOf('.')
        val decimalPart = newText.substring(dotIndex + 1)
        // 如果小数部分长度超过限制，
        if (decimalPart.length > maxDecimalDigits) {
            return if (dstart == 0 && dend == 0) {
                newText.take(dotIndex + 2)
            } else {
                ""
            }
        }
        return null
    }
}