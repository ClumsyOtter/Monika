package com.otto.common.utils

import android.text.InputFilter
import android.text.Spanned

/**
 * 更完善的 InputFilter，限制第一个字符不能是空格
 * 包含更多边界情况的处理
 */
class FirstCharacterNoSpaceFilter : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // 如果是删除操作，不限制
        if (source == null || source.isEmpty()) {
            return null
        }
        val originalText = dest?.toString() ?: ""
        val sourceText = source.toString()
        // 构建新文本
        val newText = StringBuilder(originalText).replace(dstart, dend, sourceText).toString()

        // 检查新文本是否以空格开头
        if (newText.isNotEmpty() && newText[0] == ' ') {
            // 去除开头的空格
            var index = 0
            while (index < newText.length && newText[index] == ' ') {
                index++
            }

            val trimmedText = newText.substring(index)

            // 如果整个文本都是空格
            if (trimmedText.isEmpty()) {
                return ""
            }

            // 计算需要返回的部分
            // 如果是在开头输入
            return if (dstart == 0 && dend == 0) {
                // 在开头插入
                if (index >= source.length) {
                    // 如果所有输入都是空格
                    ""
                } else {
                    // 返回去除开头空格后的部分
                    source.subSequence(index, source.length)
                }
            } else {
                // 替换操作，比较复杂，我们返回修改后的完整文本
                trimmedText
            }
        }

        return null
    }
}