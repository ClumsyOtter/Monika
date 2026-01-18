package com.otto.common.utils

object ChineseNumberConverter {

    /**
     * 中文数字字符定义
     */
    private val chineseDigits = arrayOf(
        "零", "一", "二", "三", "四", "五", "六", "七", "八", "九"
    )

    private val chineseUnits = arrayOf(
        "", "十", "百", "千"
    )

    private val chineseBigUnits = arrayOf(
        "", "万", "亿", "兆", "京", "垓", "秭", "穰", "沟", "涧", "正", "载"
    )

    /**
     * 将数字转换为中文数字（小写，支持到亿）
     * @param number 要转换的数字
     * @return 中文数字字符串
     */
    fun toChinese(number: Int): String {
        if (number == 0) return "零"

        var num = number
        val result = StringBuilder()
        var unitIndex = 0

        while (num > 0) {
            val part = num % 10000
            if (part > 0) {
                val partStr = convertPart(part)
                result.insert(0, partStr + chineseBigUnits[unitIndex])
            } else {
                // 处理连续的零
                if (result.isNotEmpty() && !result.startsWith("零")) {
                    result.insert(0, "零")
                }
            }

            num /= 10000
            unitIndex++
        }
        // 清理多余的零
        return cleanUpZeros(result.toString())
    }

    /**
     * 转换四位数字部分
     */
    private fun convertPart(part: Int): String {
        if (part == 0) return ""

        var num = part
        val result = StringBuilder()
        var unitIndex = 0

        while (num > 0) {
            val digit = num % 10
            if (digit > 0) {
                val digitStr = chineseDigits[digit]
                val unitStr = chineseUnits[unitIndex]
                result.insert(0, digitStr + unitStr)
            } else {
                // 处理零
                if (result.isNotEmpty() && !result.startsWith("零")) {
                    result.insert(0, "零")
                }
            }

            num /= 10
            unitIndex++
        }

        return cleanUpPartZeros(result.toString())
    }

    /**
     * 清理多余的零
     */
    private fun cleanUpZeros(str: String): String {
        var result = str
        // 去掉开头的零
        while (result.startsWith("零")) {
            result = result.substring(1)
        }
        // 去掉结尾的零
        while (result.endsWith("零")) {
            result = result.dropLast(1)
        }
        // 去掉中间的连续零
        result = result.replace("零零+".toRegex(), "零")
        return result
    }

    private fun cleanUpPartZeros(str: String): String {
        var result = str
        // 处理"一十"开头的情况
        if (result.startsWith("一十")) {
            result = result.substring(1)
        }
        return cleanUpZeros(result)
    }

}