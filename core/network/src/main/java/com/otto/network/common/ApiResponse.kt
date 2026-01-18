package com.otto.network.common

import com.google.gson.annotations.SerializedName

/**
 * API响应统一封装
 * 最佳实践：使用 sealed class 和 data class 组合
 */
sealed class ApiResponse<out T> {
    /**
     * 初始状态（还未发起请求）
     */
    object Initial : ApiResponse<Nothing>()

    /**
     * 加载中
     * @param message 可选的加载提示消息
     */
    data class Loading(
        val message: String = ""
    ) : ApiResponse<Nothing>()

    /**
     * 成功响应
     * @param data 响应数据，可能为 null（data 为空也属于成功）
     */
    data class Success<T>(
        val data: T?,
        val code: Int = 0,
        val message: String = ""
    ) : ApiResponse<T>()

    /**
     * 业务错误（code != 0）
     */
    data class BusinessError(
        val code: Int,
        val message: String
    ) : ApiResponse<Nothing>()

    /**
     * 网络错误
     */
    data class NetworkError(
        val throwable: Throwable
    ) : ApiResponse<Nothing>()

    /**
     * 判断是否成功
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 判断是否初始状态
     */
    val isInitial: Boolean
        get() = this is Initial

    /**
     * 判断是否加载中
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * 直接获取 data 数据
     * 如果是 Success 状态，返回 data；否则返回 null
     */
    fun getDataOrNull(): T? {
        return when (this) {
            is Success -> this.data
            else -> null
        }
    }

    /**
     * 直接获取 data 数据（非空版本）
     * 如果是 Success 状态且 data 不为 null，返回 data；否则返回默认值
     * @param default 默认值
     */
    fun getDataOrDefault(default: @UnsafeVariance T): T {
        return when (this) {
            is Success -> this.data ?: default
            else -> default
        }
    }

}

/**
 * 服务器响应数据模型
 */
data class ApiResult<T>(
    @SerializedName("code")
    val code: Int = -1,
    
    @SerializedName(value = "message", alternate = ["msg"])
    val message: String = "",
    
    @SerializedName(value = "data", alternate = ["result"])
    val data: T? = null
) {
    /**
     * 判断是否成功（通常 code == 0 表示成功）
     */
    fun isSuccess(): Boolean = code == 0

    /**
     * 转换为 ApiResponse
     * 注意：data 为空也属于成功，不应该返回 Empty
     */
    fun toApiResponse(): ApiResponse<T> {
        return when {
            isSuccess() -> ApiResponse.Success(data, code, message)
            else -> ApiResponse.BusinessError(code, message)
        }
    }
}

