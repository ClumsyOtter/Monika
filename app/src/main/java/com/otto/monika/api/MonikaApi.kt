package com.otto.monika.api

import com.otto.monika.api.common.ApiResult
import com.otto.monika.api.model.collect.request.CollectListRequest
import com.otto.monika.api.model.collect.request.CollectRequest
import com.otto.monika.api.model.collect.response.CollectListResponse
import com.otto.monika.api.model.comment.request.CommentCreateRequest
import com.otto.monika.api.model.comment.request.CommentListRequest
import com.otto.monika.api.model.comment.request.CommentRemoveRequest
import com.otto.monika.api.model.comment.request.CommentReplyRequest
import com.otto.monika.api.model.comment.request.CommentSubListRequest
import com.otto.monika.api.model.comment.request.CommentToggleLikeRequest
import com.otto.monika.api.model.comment.response.CommentItem
import com.otto.monika.api.model.comment.response.CommentListResponse
import com.otto.monika.api.model.creator.request.ApplyCreatorRequest
import com.otto.monika.api.model.creator.response.CreatorMetadataResponse
import com.otto.monika.api.model.login.request.NvsLoginRequest
import com.otto.monika.api.model.login.request.PhoneBindRequest
import com.otto.monika.api.model.login.request.PhoneLoginRequest
import com.otto.monika.api.model.login.request.PhoneVerifyRequest
import com.otto.monika.api.model.login.request.SmsCodeRequest
import com.otto.monika.api.model.login.request.WechatLoginRequest
import com.otto.monika.api.model.login.responese.PhoneLoginResponse
import com.otto.monika.api.model.login.responese.PhoneVerifyResponse
import com.otto.monika.api.model.login.responese.VisitorResponse
import com.otto.monika.api.model.pay.request.CheckPayStatusRequest
import com.otto.monika.api.model.pay.request.CreateOrderRequest
import com.otto.monika.api.model.pay.request.TestCreateOrderRequest
import com.otto.monika.api.model.pay.response.CheckPayStatusResponse
import com.otto.monika.api.model.pay.response.CreateOrderResponse
import com.otto.monika.api.model.pay.response.TestCreateOrderResponse
import com.otto.monika.api.model.post.request.PostDetailRequest
import com.otto.monika.api.model.post.request.PostLikeRequest
import com.otto.monika.api.model.post.request.PostListRequest
import com.otto.monika.api.model.post.request.PostRemoveRequest
import com.otto.monika.api.model.post.response.PostItem
import com.otto.monika.api.model.post.response.PostListResponse
import com.otto.monika.api.model.post.response.TagListResponse
import com.otto.monika.api.model.subscribe.request.MyCreatorListRequest
import com.otto.monika.api.model.subscribe.request.SubscribePlanListRequest
import com.otto.monika.api.model.subscribe.request.SubscribeUserListRequest
import com.otto.monika.api.model.subscribe.response.MyCreatorListResponse
import com.otto.monika.api.model.subscribe.response.SubscribePlanCreateResponse
import com.otto.monika.api.model.subscribe.response.SubscribePlanListResponse
import com.otto.monika.api.model.subscribe.response.SubscribeUserListResponse
import com.otto.monika.api.model.user.request.EditUserRequest
import com.otto.monika.api.model.user.request.UserProfileRequest
import com.otto.monika.api.model.user.response.MonikaUserInfoModel
import com.otto.monika.api.model.user.response.UserIncomeResponse
import com.otto.monika.api.network.host.HOST
import com.otto.monika.home.model.MonikaBannerData
import com.otto.monika.home.model.MonikaIntroduceData
import com.otto.monika.home.model.MonikaPostData
import com.otto.monika.home.model.MonikaRankData
import com.otto.monika.home.model.MonikaSubscribeData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * CY API 接口定义
 * 使用最佳实践：
 * 1. 返回类型使用 ApiResult<T> 统一封装
 * 2. 支持协程（suspend fun）和回调（Call）两种方式
 * 3. 使用 ApiResponse 进行结果处理
 */
@HOST(releaseUrl = "https://service.ciya.club", testUrl = "https://service-test.ciya.club")
interface MonikaApi {

    @POST("/app/login/asVisitor")
    suspend fun asVisitor(): Response<ApiResult<VisitorResponse>>

    /**
     * 获取短信验证码
     * @param request 请求参数，包含手机号
     * @return 响应结果，data 为 null
     */
    @POST("/app/getSmsCode")
    suspend fun getSmsCode(@Body request: SmsCodeRequest): Response<ApiResult<Unit>>

    /**
     * 通过短信验证码登录
     * @param request 请求参数，包含手机号和验证码
     * @return 响应结果，包含 token
     */
    @POST("/app/login/viaCaptcha")
    suspend fun loginViaCaptcha(@Body request: PhoneLoginRequest): Response<ApiResult<PhoneLoginResponse>>

    /**
     * 验证手机号
     * @param request 请求参数，包含手机号和验证码
     * @return 响应结果，包含 acToken
     */
    @POST("/app/user/phone/verify")
    suspend fun verifyPhone(@Body request: PhoneVerifyRequest): Response<ApiResult<PhoneVerifyResponse>>

    /**
     * 绑定手机号
     * @param request 请求参数，包含旧手机号、新手机号、验证码和 acToken
     * @return 响应结果，data 为 true 表示绑定成功
     */
    @POST("/app/user/phone/bind")
    suspend fun bindPhone(@Body request: PhoneBindRequest): Response<ApiResult<Boolean>>

    /**
     * 通过微信登录
     * @param request 请求参数，包含 openId、nickname、wxHeadImage
     * @return 响应结果，包含 token
     */
    @POST("/app/login/viaWechat")
    suspend fun loginViaWechat(@Body request: WechatLoginRequest): Response<ApiResult<PhoneLoginResponse>>

    @POST("/app/login/viaOneClick")
    suspend fun nvsLogin(@Body request: NvsLoginRequest): Response<ApiResult<VisitorResponse>>

    /**
     * 获取首页推荐列表
     * @return 响应结果，包含推荐列表数据
     */
    @POST("app/post/recommendList")
    suspend fun homeRecommendList(): Response<ApiResult<MonikaIntroduceData>>

    /**
     * 获取用户信息
     * @param request 请求参数，包含可选的 uid（如果传 uid，则查询指定用户；如果不传 uid，则查询当前登录用户）
     * @return 响应结果，包含用户信息
     *
     * 返回说明：
     * - 查询自己时返回完整信息（包括私密信息如手机号、openid、设备ID等）
     * - 查询别人时只返回公开信息（不包含手机号等私密信息），但会返回是否收藏该用户
     * - 如果查询的是创作者，则返回创作者相关信息（粉丝数、擅长领域、自我标签等）
     * - 如果是查询自己且是审核通过的创作者，则返回收入统计
     */
    @POST("/app/user/profile")
    suspend fun getUserProfile(@Body request: UserProfileRequest): Response<ApiResult<MonikaUserInfoModel>>

    /**
     * 编辑用户信息
     * @param request 请求参数，包含昵称和头像URL
     * @return 响应结果，data 为 true 表示修改成功
     */
    @POST("/app/user/edit")
    suspend fun editUser(@Body request: EditUserRequest): Response<ApiResult<Boolean>>

    /**
     * 获取用户收入信息
     * @return 响应结果，包含总收入、未结算收入、已结算收入、已提现金额、可提现金额
     */
    @POST("/app/user/income")
    suspend fun getUserIncome(): Response<ApiResult<UserIncomeResponse>>

    /**
     * 根据 ID 列表获取帖子详情
     * @param postIds 帖子 ID 列表（字符串格式）
     * @return 响应结果，包含帖子详情列表
     */
    @POST("app/post/getListByIds")
    @FormUrlEncoded
    suspend fun getListByIds(@Field("postIds") postIds: String): Response<ApiResult<MonikaPostData>>

    /**
     * 首页 Banner 数据
     */
    @GET("app/banner/list")
    suspend fun homeBannerList(): Response<ApiResult<MonikaBannerData>>

    /*
     * 获取创作者申请页面的元数据
     * @return 响应结果，包含擅长领域、自我标签、社交媒体列表
     */
    @POST("/app/creator/metadata")
    suspend fun getCreatorMetadata(): Response<ApiResult<CreatorMetadataResponse>>

    /**
     * 申请成为创作者
     * @param request 请求参数，包含用户信息、擅长领域、自我标签、社交媒体等
     * @return 响应结果，data 为 true 表示申请成功
     */
    @POST("/app/creator/apply")
    suspend fun applyCreator(@Body request: ApplyCreatorRequest): Response<ApiResult<Boolean>>

    /**
     * 首页 我的订阅
     */
    @POST("app/post/subscribePostList")
    @FormUrlEncoded
    suspend fun subscribePostList(
        @Field("page") page: Int,
        @Field("pageSize") pageSize: Int
    ): Response<ApiResult<MonikaSubscribeData>>

    /**
     * 首页 订阅排名
     */
    @POST("app/subscribe/ranking/home")
    @FormUrlEncoded
    suspend fun homeRanking(
        @Field("limit") limit: Int,
        @Field("statDate") statDate: String?
    ): Response<ApiResult<MonikaRankData>>

    @POST("app/getUploadUrl")
    @FormUrlEncoded
    suspend fun getUploadUrl(@Field("files") files: String): Response<ApiResult<Map<String, String>>>


    /**
     * 获取订阅方案列表
     * @param request 请求参数，包含 uid、page、pageSize
     * @return 响应结果，包含订阅方案列表
     */
    @POST("/app/subscribe/plan/list")
    suspend fun getSubscribePlanList(@Body request: SubscribePlanListRequest): Response<ApiResult<SubscribePlanListResponse>>

    /**
     * 批量创建订阅方案
     * @param plans 请求参数，包含订阅方案列表（JSON 字符串）
     * @return 响应结果，包含 added、updated、deleted 统计信息
     */
    @POST("/app/subscribe/plan/create")
    @FormUrlEncoded
    suspend fun createSubscribePlans(@Field("plans") plans: String): Response<ApiResult<SubscribePlanCreateResponse>>


    /**
     * 删除订阅方案
     */
    @POST("/app/subscribe/plan/delete")
    @FormUrlEncoded
    suspend fun deleteSubscribePlan(@Field("id") id: String): Response<ApiResult<Boolean>>

    /**
     * 获取订阅用户列表
     * @param request 请求参数，包含 uid、page、pageSize
     * @return 响应结果，包含订阅用户列表和总数
     */
    @POST("/app/subscribe/userList")
    suspend fun getSubscribeUserList(@Body request: SubscribeUserListRequest): Response<ApiResult<SubscribeUserListResponse>>

    /**
     * 获取帖子列表
     * @param request 请求参数，包含 uid、page、pageSize
     * @return 响应结果，包含帖子列表和总数
     */
    @POST("/app/post/subscribePostList")
    suspend fun getSubscriberPostList(@Body request: PostListRequest): Response<ApiResult<PostListResponse>>


    /**
     * 获取用户订阅的创作者的帖子列表
     * @param request 请求参数，包含 uid、page、pageSize
     * @return 响应结果，包含帖子列表和总数
     */
    @POST("/app/post/postList")
    suspend fun getPostList(@Body request: PostListRequest): Response<ApiResult<PostListResponse>>

    /**
     * 获取帖子详情
     * @param request 请求参数，包含 postId
     * @return 响应结果，包含帖子详情
     */
    @POST("/app/post/detail")
    suspend fun getPostDetail(@Body request: PostDetailRequest): Response<ApiResult<PostItem>>

    /**
     * 获取评论列表
     * @param request 请求参数，包含 postId、uid、page、pageSize
     * @return 响应结果，包含评论列表和总数
     */
    @POST("/app/comment/list")
    suspend fun getCommentList(@Body request: CommentListRequest): Response<ApiResult<CommentListResponse>>

    /**
     * 获取二级评论列表
     * @param request 请求参数，包含 parentId、postId、uid、page、pageSize
     * @return 响应结果，包含二级评论列表和总数
     */
    @POST("/app/comment/subList")
    suspend fun getCommentSubList(@Body request: CommentSubListRequest): Response<ApiResult<CommentListResponse>>

    /**
     * 创建评论
     */
    @POST("/app/comment/create")
    suspend fun createComment(@Body request: CommentCreateRequest): Response<ApiResult<CommentItem>>

    /**
     * 回复评论
     */
    @POST("/app/comment/reply")
    suspend fun replyComment(@Body request: CommentReplyRequest): Response<ApiResult<CommentItem>>

    /**
     * 评论点赞/取消点赞
     */
    @POST("/app/comment/toggleLike")
    suspend fun toggleCommentLike(@Body request: CommentToggleLikeRequest): Response<ApiResult<Unit>>

    /**
     * 删除评论
     */
    @POST("/app/comment/remove")
    suspend fun removeComment(@Body request: CommentRemoveRequest): Response<ApiResult<Unit>>

    /**
     * 获取我的订阅用户列表
     * @param request 请求参数，包含 uid、page、pageSize
     * @return 响应结果，包含订阅用户列表和总数
     */
    @POST("/app/subscribe/myCreatorList")
    suspend fun getMyCreatorList(@Body request: MyCreatorListRequest): Response<ApiResult<MyCreatorListResponse>>

    /**
     * 获取收藏列表
     * @param request 请求参数，包含 target_type、createdAtStart、createdAtEnd、page、pageSize
     * @return 响应结果，包含收藏列表和总数
     */
    @POST("/app/collect/list")
    suspend fun getCollectList(@Body request: CollectListRequest): Response<ApiResult<CollectListResponse>>

    /**
     * 点赞帖子
     * @param request 请求参数，包含 postId
     * @return 响应结果，data 为 true 表示点赞成功
     */
    @POST("/app/post/like/add")
    suspend fun addPostLike(@Body request: PostLikeRequest): Response<ApiResult<Boolean>>

    /**
     * 取消点赞帖子
     * @param request 请求参数，包含 postId
     * @return 响应结果，data 为 true 表示取消点赞成功
     */
    @POST("/app/post/like/remove")
    suspend fun removePostLike(@Body request: PostLikeRequest): Response<ApiResult<Boolean>>

    /**
     * 删除帖子
     * @param request 请求参数，包含 postId
     * @return 响应结果，data 为 true 表示删除成功
     */
    @POST("/app/post/remove")
    suspend fun removePost(@Body request: PostRemoveRequest): Response<ApiResult<Boolean>>

    /**
     * 订阅排行榜
     */
    @POST("app/subscribe/ranking/stats")
    @FormUrlEncoded
    suspend fun rankingStats(
        @Field("limit") limit: Int,
        @Field("statType") statType: Int,
        @Field("statDate") statDate: String?
    ): Response<ApiResult<MonikaRankData>>

    /**
     * 收藏
     * @param request 请求参数，包含 target_type 和 target_id
     * @return 响应结果，data 为 true 表示收藏成功
     */
    @POST("/app/collect/add")
    suspend fun addCollect(@Body request: CollectRequest): Response<ApiResult<Boolean>>

    /**
     * 取消收藏
     * @param request 请求参数，包含 target_type 和 target_id
     * @return 响应结果，data 为 true 表示取消收藏成功
     */
    @POST("/app/collect/remove")
    suspend fun removeCollect(@Body request: CollectRequest): Response<ApiResult<Boolean>>

    /**
     * 创建订单
     * @param request 请求参数，包含 subscribePlanId 和 duration
     * @return 响应结果，包含订单信息（serial_number、orderNo、appid、channels）
     */
    @POST("/app/pay/createOrder")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResult<CreateOrderResponse>>

    /**
     * 检查支付状态
     * @param request 请求参数，包含 orderNo
     * @return 响应结果，包含 isPay（是否支付成功）
     */
    @POST("/app/pay/isPaySuccess")
    suspend fun checkPayStatus(@Body request: CheckPayStatusRequest): Response<ApiResult<CheckPayStatusResponse>>

    /**
     * 模拟创建订单（测试用）
     * @param request 请求参数，包含 uid、subscribePlanId 和 duration
     * @return 响应结果，包含 order_id 和 callback_result
     */
    @POST("/app/pay/testCreate")
    suspend fun testCreateOrder(@Body request: TestCreateOrderRequest): Response<ApiResult<TestCreateOrderResponse>>

    /**
     * 退出登录
     * @return 响应结果，data 为 null
     */
    @POST("/app/login/quit")
    suspend fun logout(): Response<ApiResult<Unit>>


    /**
     * 获取标签列表
     * @return 响应结果，包含标签列表
     */
    @POST("/app/post/tags")
    suspend fun getTags(): Response<ApiResult<TagListResponse>>

    @POST("app/post/create")//可见类型，0所有人可见，1主页可见，2订阅者可见
    @FormUrlEncoded
    suspend fun createPost(
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("topic") topic: String?,
        @Field("tags") tags: String?,
        @Field("images") images: String?,
        @Field("visible_type") visibleType: Int
    ): Response<ApiResult<MonikaRankData>>
}