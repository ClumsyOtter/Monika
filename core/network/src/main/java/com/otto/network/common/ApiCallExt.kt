package com.otto.network.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


/**
 * 将 Response 转换为 ApiResponse
 * 适用于协程方式调用的接口
 */
fun <T> Response<ApiResult<T>>.toApiResponse(): ApiResponse<T> {
    return try {
        if (isSuccessful) {
            val body = body()
            // 如果响应体为空，返回 NetworkError（HTTP 成功但响应体为空属于异常情况）
            body?.toApiResponse() ?: ApiResponse.NetworkError(
                IllegalStateException("响应体为空")
            )
        } else {
            ApiResponse.NetworkError(HttpException(this))
        }
    } catch (e: Exception) {
        ApiResponse.NetworkError(e)
    }
}


/**
 * 将 suspend 函数转换为 Flow，自动发送 Loading 状态
 * 适用于协程 suspend 函数
 */
fun <T> (suspend () -> Response<ApiResult<T>>).asFlow(): Flow<ApiResponse<T>> = flow {
    // 先发送 Loading 状态
    emit(ApiResponse.Loading())
    // 执行请求
    val response = try {
        invoke()
    } catch (e: IOException) {
        emit(ApiResponse.NetworkError(e))
        return@flow
    } catch (e: Exception) {
        emit(ApiResponse.NetworkError(e))
        return@flow
    }
    // 发送结果
    emit(response.toApiResponse())
}.flowOn(Dispatchers.IO)

/**
 * 简化的状态监听器接口
 * 只关注 loading、success、fail 三种状态
 */
interface SimpleApiListener<T> {
    /**
     * 加载中回调（可选实现）
     */
    fun onLoading() {}

    /**
     * 成功回调，接收数据
     * @param data 响应数据，可能为 null
     */
    fun onSuccess(data: T?)

    /**
     * 失败回调，接收错误消息
     * @param message 错误消息
     */
    fun onFail(message: String)
}

/**
 * 简化的 collect 方法，只关注 loading、success、fail 三种状态
 *
 * @param listener 状态监听器，包含三个回调方法
 *
 * 使用示例：
 * ```kotlin
 * viewModel.getSmsCodeFlow(phone).collectSimple(object : SimpleApiListener<Unit> {
 *     override fun onLoading() {
 *         showLoading()
 *     }
 *
 *     override fun onSuccess(data: Unit?) {
 *         hideLoading()
 *         // 处理成功逻辑
 *     }
 *
 *     override fun onFail(message: String) {
 *         hideLoading()
 *         Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
 *     }
 * })
 * ```
 */
suspend fun <T> Flow<ApiResponse<T>>.collectSimple(listener: SimpleApiListener<T>) {
    collect { response ->
        when (response) {
            is ApiResponse.Loading -> {
                listener.onLoading()
            }

            is ApiResponse.Success -> {
                listener.onSuccess(response.data)
            }

            is ApiResponse.BusinessError -> {
                listener.onFail(response.message)
            }

            is ApiResponse.NetworkError -> {
                listener.onFail("网络错误: ${response.throwable.message ?: "未知错误"}")
            }

            is ApiResponse.Initial -> {
                // 初始状态，不做处理
            }
        }
    }
}

/**
 * 简化的 collect 方法，只关注 loading、success、fail 三种状态
 *
 * @param onLoading 加载中回调（可选）
 * @param onSuccess 成功回调，接收数据
 * @param onFail 失败回调，接收错误消息
 *
 * 使用示例：
 * ```kotlin
 * viewModel.getSmsCodeFlow(phone).collectSimple(
 *     onLoading = { showLoading() },
 *     onSuccess = { data ->
 *         hideLoading()
 *         // 处理成功逻辑
 *     },
 *     onFail = { message ->
 *         hideLoading()
 *         Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
 *     }
 * )
 * ```
 */
suspend fun <T> Flow<ApiResponse<T>>.collectSimple(
    onLoading: (() -> Unit)? = null,
    onSuccess: (T?) -> Unit,
    onFailure: (String) -> Unit
) {
    collect { response ->
        when (response) {
            is ApiResponse.Loading -> {
                onLoading?.invoke()
            }

            is ApiResponse.Success -> {
                onSuccess(response.data)
            }

            is ApiResponse.BusinessError -> {
                onFailure(response.message)
            }

            is ApiResponse.NetworkError -> {
                onFailure("网络错误: ${response.throwable.message ?: "未知错误"}")
            }

            is ApiResponse.Initial -> {
                // 初始状态，不做处理
            }
        }
    }
}

fun <T, R> ApiResponse<T>.transform(transformer: (T?) -> Flow<ApiResponse<R>>): Flow<ApiResponse<R>> {
    return when (this@transform) {
        is ApiResponse.Loading -> {
            flowOf(ApiResponse.Loading())
        }

        is ApiResponse.Success -> {
            transformer.invoke(this@transform.data)
        }

        is ApiResponse.BusinessError -> {
            flowOf(ApiResponse.BusinessError(this@transform.code, this@transform.message))
        }

        is ApiResponse.NetworkError -> {
            flowOf(ApiResponse.NetworkError(this@transform.throwable))
        }

        is ApiResponse.Initial -> {
            flowOf(ApiResponse.Initial)
        }
    }
}

/**
 * 顺序执行多个 API 请求，自动处理 Loading 和错误状态
 * 每个 API 会等待前一个 API 完成后再执行，失败时立即停止后续执行
 *
 * @param apiCalls 要顺序执行的 API 调用列表
 * @return Flow，按顺序发送每个 API 的结果
 *
 * 使用示例：
 * ```kotlin
 * // 在 ViewModel 中使用
 * fun loadDataSequentially(): Flow<ApiResponse<*>> {
 *     return sequentialApiFlow(
 *         suspend { api.getFirstData() },
 *         suspend { api.getSecondData() },
 *         suspend { api.getThirdData() }
 *     )
 * }
 *
 * // 在 Activity/Fragment 中收集
 * lifecycleScope.launch {
 *     viewModel.loadDataSequentially().collect { response ->
 *         when (response) {
 *             is ApiResponse.Loading -> showLoading()
 *             is ApiResponse.Success -> {
 *                 // 处理成功，注意：这里会收到每个 API 的结果
 *                 // 可以通过判断 response.data 的类型来区分是哪个 API 的结果
 *             }
 *             is ApiResponse.BusinessError -> {
 *                 hideLoading()
 *                 showError(response.message)
 *             }
 *             is ApiResponse.NetworkError -> {
 *                 hideLoading()
 *                 showError("网络错误")
 *             }
 *             else -> {}
 *         }
 *     }
 * }
 * ```
 */

/**
 * 顺序执行多个 API 请求，使用 flatMapConcat 连接
 * 适用于需要根据前一个 API 的结果来决定下一个 API 的场景
 *
 * 使用示例：
 * ```kotlin
 * // 方式1：使用 flatMapConcat 顺序执行
 * fun loadDataWithFlatMap(): Flow<ApiResponse<*>> {
 *     return flowOf(Unit)
 *         .flatMapConcat {
 *             suspend { api.getFirstData() }.asFlow()
 *         }
 *         .flatMapConcat { firstResponse ->
 *             // 根据第一个 API 的结果调用第二个 API
 *             if (firstResponse is ApiResponse.Success && firstResponse.data != null) {
 *                 suspend { api.getSecondData(firstResponse.data.id) }.asFlow()
 *             } else {
 *                 flow { emit(firstResponse) }  // 如果第一个失败，直接返回
 *             }
 *         }
 * }
 *
 * // 方式2：在 flow 构建器中顺序调用
 * fun loadDataInFlow(): Flow<ApiResponse<*>> = flow {
 *     emit(ApiResponse.Loading())
 *
 *     // 第一个 API
 *     val firstResponse = try {
 *         api.getFirstData()
 *     } catch (e: Exception) {
 *         emit(ApiResponse.NetworkError(e))
 *         return@flow
 *     }
 *     val firstApiResponse = firstResponse.toApiResponse()
 *     emit(firstApiResponse)
 *
 *     // 如果第一个成功，调用第二个 API
 *     if (firstApiResponse is ApiResponse.Success) {
 *         val firstData = firstApiResponse.data
 *         if (firstData != null) {
 *             val secondResponse = try {
 *                 api.getSecondData(firstData.id)
 *             } catch (e: Exception) {
 *                 emit(ApiResponse.NetworkError(e))
 *                 return@flow
 *             }
 *             emit(secondResponse.toApiResponse())
 *         }
 *     }
 * }.flowOn(Dispatchers.IO)
 * ```
 */

