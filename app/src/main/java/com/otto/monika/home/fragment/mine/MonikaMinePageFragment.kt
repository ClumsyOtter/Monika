package com.otto.monika.home.fragment.mine

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.otto.monika.R
import com.otto.monika.account.creator.ApplyToCreatorActivity
import com.otto.monika.account.edit.AccountEditActivity
import com.otto.monika.account.income.AccountIncomeActivity
import com.otto.monika.account.rank.AccountSubscribeRankActivity
import com.otto.monika.account.rank.fragment.RankSource
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.views.MonikaTabItem
import com.otto.monika.home.fragment.BaseUserLogicFragment
import com.otto.monika.home.fragment.EmptyContentFragment
import com.otto.monika.home.fragment.mine.listener.AccountHeadListener
import com.otto.monika.home.fragment.mine.listener.TabCountListener
import com.otto.monika.home.fragment.mine.listener.TabSelectListener
import com.otto.monika.home.fragment.mine.model.CollectCreatorEvent
import com.otto.monika.home.fragment.mine.model.TabConfig
import com.otto.monika.home.fragment.mine.model.TabConfigManager
import com.otto.monika.home.fragment.mine.viewmodel.MonikaMineViewModel
import com.otto.monika.home.fragment.post.UserPostListFragment
import com.otto.monika.post.publish.MonikaPublishPostActivity
import com.otto.monika.setting.MonikaSettingActivity
import com.otto.monika.subscribe.plan.SubscribePlanActivity
import com.otto.monika.subscribe.support.SubscriptionSupportActivity
import com.otto.network.common.collectSimple
import com.otto.network.model.user.response.AccountType
import com.otto.network.model.user.response.MonikaUserInfoModel
import com.otto.network.model.user.response.getAccountType
import com.otto.network.model.user.response.isCreator
import com.otto.network.model.user.response.isSelf
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.math.max

class MonikaMinePageFragment : BaseUserLogicFragment() {

    private val viewModel: MonikaMineViewModel by viewModels()
    private val fragments = mutableListOf<MonikaBaseFragment>()
    private val tabList = mutableListOf<MonikaTabItem>()

    // 保存 Tab 的原始标题，用于更新时拼接数量
    private val tabOriginalTitles = mutableMapOf<Int, String>()

    // 当前用户类型（用于判断是否需要重新生成 Tab）
    private var currentAccountType: AccountType? = null

    // Activity Result Launchers
    private val applyToCreatorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 申请创作者成功，刷新用户信息
            loadAccountData()
        }
    }

    private val accountEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 编辑成功，刷新用户信息
            loadAccountData()
        }
    }

    private val accountSubscribeRankLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 粉丝列表页面，通常不需要处理返回结果
        // 如果需要，可以在这里处理
    }

    private val accountIncomeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 收入页面，通常不需要处理返回结果
        // 如果需要，可以在这里处理
    }

    private val subscribePlanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 订阅方案可能有变化，刷新用户信息
            //loadAccountData()
        }
    }

    private val subscriptionSupportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 订阅成功，刷新用户信息
            loadAccountData()
        }
    }

    private val publishPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                (fragments.find { it is UserPostListFragment } as? UserPostListFragment)?.postListChanged()
            }
        }

    companion object {
        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_ACTION_BAR = "arg_show_action_bar"

        @JvmStatic
        fun newInstance(
            userId: String? = null,
            showActionBar: Boolean = false
        ): MonikaMinePageFragment {
            return MonikaMinePageFragment().apply {
                arguments = Bundle().apply {
                    userId?.let { putString(ARG_USER_ID, it) }
                    putBoolean(ARG_ACTION_BAR, showActionBar)
                }
            }
        }
    }

    /**
     * 获取用户ID
     */
    private fun getUserId(): String? {
        return arguments?.getString(ARG_USER_ID)
    }


    /**
     * 获取是否显示actionBar
     */
    private fun showActionbar(): Boolean? {
        return arguments?.getBoolean(ARG_ACTION_BAR)
    }

    private fun onTabSelected(position: Int, pageChange: Boolean = false) {
        tabList.forEachIndexed { index, monikaTabItem ->
            monikaTabItem.setSelected(position == index)
            if (pageChange) {
                myPageBinding.viewPager.setCurrentItem(position, true)
            }
        }
        fragments.forEachIndexed { index, fragment ->
            (fragment as? TabSelectListener)?.onFragmentSelected(index == position)
        }
    }

    private fun initListener() {
        initAccountInfoListener()
        myPageBinding.accountCreatorOptionSetting.onOptionClickListener = {
            Toast.makeText(requireActivity(), "设置", Toast.LENGTH_SHORT).show()
            MonikaSettingActivity.enter(requireActivity())
        }

        // 发动态悬浮按钮点击事件
        myPageBinding.ivAccountAddPost.setOnClickListener {
            handlePublishDynamicClick()
        }
        // 收藏/取消收藏点击事件
        myPageBinding.monikaAccountBottomBar.onCollectionClick = { userInfo ->
            // 根据用户收藏状态触发收藏或取消收藏
            handleUserCollect(userInfo)
        }
        myPageBinding.monikaAccountBottomBar.onSubscribeClick = { userInfo ->
            // 使用 Activity Result API 启动订阅支持页面
            val intent = SubscriptionSupportActivity.getIntent(requireContext(), userInfo?.uid)
            subscriptionSupportLauncher.launch(intent)
        }
    }

    /**
     * 处理发动态按钮点击
     */
    private fun handlePublishDynamicClick() {
        publishPostLauncher.launch(MonikaPublishPostActivity.getIntent(requireActivity()))
    }

    private fun initAccountInfoListener() {
        myPageBinding.viewAccountHead.setAccountHeadListener(object : AccountHeadListener() {
            override fun onApplyToCreatorClick(profileResponse: MonikaUserInfoModel) {
                applyToCreatorLauncher.launch(
                    ApplyToCreatorActivity.getIntent(
                        requireActivity(),
                        profileResponse
                    )
                )
            }

            override fun onEditNameClick(profileResponse: MonikaUserInfoModel) {
                accountEditLauncher.launch(
                    AccountEditActivity.getIntent(
                        requireActivity(),
                        profileResponse
                    )
                )
            }

            override fun onMyFansClick(profileResponse: MonikaUserInfoModel) {
                accountSubscribeRankLauncher.launch(
                    AccountSubscribeRankActivity.getIntent(
                        requireActivity(),
                        profileResponse.uid,
                        RankSource.ACCOUNT
                    )
                )
            }

            override fun onMyIncomeClick(profileResponse: MonikaUserInfoModel) {
                accountIncomeLauncher.launch(
                    AccountIncomeActivity.getIntent(
                        requireActivity(),
                        profileResponse.uid
                    )
                )
            }

            override fun onSubscribePlanClick(profileResponse: MonikaUserInfoModel) {
                subscribePlanLauncher.launch(
                    SubscribePlanActivity.getIntent(
                        requireActivity(),
                        profileResponse.uid
                    )
                )
            }

            override fun onUploadPostClick(profileResponse: MonikaUserInfoModel) {
                Toast.makeText(requireActivity(), "上传作品", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onFinishCreateView() {
        setupActionBarWindowInsets()
        initListener()
        initBottomSheetBehavior()
        setupUiState()
        loadAccountData()
    }

    override fun onFragmentSelected(isSelected: Boolean) {
        super.onFragmentSelected(isSelected)
        if (isSelected) {
            runCatching {
                //用户重新进入之后，向子fragment传递选中信息
                val position = myPageBinding.viewPager.currentItem
                fragments.forEachIndexed { index, fragment ->
                    (fragment as? TabSelectListener)?.onFragmentSelected(index == position)
                }
            }
        }
    }

    fun initBottomSheetBehavior() {
        myPageBinding.dropFrameView.post {
            initBottomSheetBehavior(hasActionBar = showActionbar() ?: false)
        }
    }

    override fun onNavBarAlphaChange(value: Float) {
        super.onNavBarAlphaChange(value)
        if (showActionbar() == true) {
            if (value.toInt() == 0) {
                myPageBinding.monikaAccountActionBar.fullShow(false)
            } else {
                myPageBinding.monikaAccountActionBar.fullShow(true)
            }
        }
    }

    /**
     * 设置ActionBar的WindowInsets，向下padding状态栏高度
     */
    private fun setupActionBarWindowInsets() {
        this@MonikaMinePageFragment.myPageBinding.monikaAccountActionBar.isVisible = showActionbar() ?: false
        myPageBinding.monikaAccountActionBar.onBackClickListener = {
            requireActivity().finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(myPageBinding.monikaAccountActionBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 获取状态栏高度
            val statusBarHeight = systemBars.top
            // 设置paddingTop，将ActionBar向下调整
            view.setPadding(
                view.paddingLeft,
                statusBarHeight,
                view.paddingRight,
                view.paddingBottom
            )
            // 返回消耗的insets
            insets
        }
    }


    fun loadAccountData() {
        viewModel.loadUserProfile(getUserId())
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 观察账户数据
        // 使用 viewLifemonikacleOwner 避免内存泄漏，使用 repeatOnLifemonikacle 确保只在可见时收集
        lifecycleScope.launch {
            viewModel.userProfileState.collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { data ->
                    hideLoadingDialog()
                    if (data != null) {
                        myPageBinding.ivAccountAddPost.isVisible = data.isSelf() && data.isCreator()
                        myPageBinding.monikaAccountBottomBar.setCyUserInfoModel(data)
                        myPageBinding.accountCreatorOption.isVisible = data.isSelf()
                        myPageBinding.monikaAccountActionBar.setAvatar(data.avatar)
                        myPageBinding.monikaAccountActionBar.setName(data.nickname)
                        myPageBinding.viewAccountHead.setProfileResponse(data)
                        // 根据用户属性动态生成 Tab 和 Fragment（仅在用户类型改变时重新生成）
                        setupTabsAndFragments(data)
                        myPageBinding.accountBackdropWall.apply {
                            setImageList(data.collectBg)
                        }
                        //数据更新之后高度会变化，所以做适当延迟更新peekHeight
                        myPageBinding.dropFrameView.postDelayed({
                            updatePeekHeight(hasActionBar = showActionbar() ?: false)
                        }, 100)
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    /**
     * 处理用户收藏/取消收藏
     */
    private fun handleUserCollect(userInfo: MonikaUserInfoModel?) {
        val targetId = userInfo?.uid ?: return
        val isCollected = userInfo.isCollected == true

        lifecycleScope.launch {
            val collectFlow = if (isCollected) {
                // 当前已收藏，执行取消收藏
                viewModel.removeCollectFlow(targetId)
            } else {
                // 当前未收藏，执行收藏
                viewModel.addCollectFlow(targetId)
            }
            collectFlow.collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { success ->
                    hideLoadingDialog()
                    if (success == true) {
                        // 收藏/取消收藏成功，更新收藏状态
                        viewModel.userProfileState.value.getDataOrNull()?.let { userInfoModel ->
                            userInfoModel.isCollected = isCollected.not()
                            val currentCollectNum = userInfoModel.collectNum ?: 0
                            userInfoModel.collectNum = if (isCollected) {
                                max(currentCollectNum - 1, 0)
                            } else {
                                currentCollectNum + 1
                            }
                            myPageBinding.monikaAccountBottomBar.setCyUserInfoModel(userInfoModel)
                            //发送消息通知
                            EventBus.getDefault().post(
                                CollectCreatorEvent(
                                    creatorUid = userInfo.uid,
                                    isCollected = userInfoModel.isCollected
                                )
                            )
                        }
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    /**
     * 根据用户属性设置 Tab 和 Fragment
     */
    private fun setupTabsAndFragments(profileResponse: MonikaUserInfoModel) {
        val accountType = profileResponse.getAccountType()

        // 如果用户类型没有改变，不需要重新生成
        if (currentAccountType == accountType) {
            return
        }

        // 更新当前用户类型
        currentAccountType = accountType

        // 生成 Tab 配置，传入 uid
        val uid = profileResponse.uid
        val tabConfigs = uid?.let {
            TabConfigManager.generateMineTabConfigs(
                accountType = accountType,
                uid = it,
                isOwner = profileResponse.isSelf()
            )
        }
        // 清空现有的 Tab
        myPageBinding.llAccountTabsContainer.removeAllViews()
        tabList.clear()
        // 隐藏 Tab 容器（如果没有 tab 配置）
        (myPageBinding.llAccountTabsContainer.parent as? android.view.View)?.isVisible =
            tabConfigs?.isNotEmpty() == true
        // 创建 Fragment 列表
        fragments.clear()
        // 如果没有 Tab 配置，添加空白 Fragment
        if (tabConfigs?.isEmpty() == true) {
            fragments.add(EmptyContentFragment())
        } else {
            // 动态创建 Tab
            tabConfigs?.forEachIndexed { index, tabConfig ->
                val tabItem = generateTabItem(index, tabConfig)
                myPageBinding.llAccountTabsContainer.addView(tabItem)
                tabList.add(tabItem)
                // 保存原始标题
                tabOriginalTitles[index] = tabConfig.title
                // 设置 Tab 点击事件
                tabItem.setOnClickListener {
                    onTabSelected(index, true)
                }
                // 创建对应的 Fragment
                val fragment = tabConfig.fragmentFactory()
                fragments.add(fragment)
                // 如果 Fragment 实现了 TabCountListener 接口，设置数量变化回调
                if (fragment is TabCountListener) {
                    setupTabCountListener(index, fragment)
                }
            }
        }
        // 设置 ViewPager 适配器
        val pageAdapter = ContentPageAdapter(childFragmentManager, lifecycle, fragments)
        myPageBinding.viewPager.adapter = pageAdapter
        myPageBinding.viewPager.offscreenPageLimit = fragments.size.coerceAtMost(4)
        // 设置 ViewPager 页面改变监听（仅在有 tab 时）
        if (tabConfigs?.isNotEmpty() == true) {
            myPageBinding.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(p0: Int) {
                    onTabSelected(p0)
                }
            })

            // 默认选中第一个 Tab
            onTabSelected(0, true)
        }
    }

    private fun generateTabItem(index: Int, tabConfig: TabConfig): MonikaTabItem {
        // 直接创建并手动设置样式
        val tabItem = MonikaTabItem(requireContext()).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                if (index > 0) {
                    marginStart = resources.getDimensionPixelSize(R.dimen.dimen_20dp)
                }
            }
        }

        // 等待 View 初始化完成后设置标题和样式
        tabItem.post {
            // 设置标题
            tabItem.setSubTitle(tabConfig.title)
            // 设置文字颜色
            tabItem.setSubTitleTextColor(
                resources.getColor(R.color.text_000000, null),
                resources.getColor(R.color.text_50000000, null)
            )
            // 设置文字大小
            tabItem.setSubTitleTextSize(14f, 18f)
            // 设置指示器
            tabItem.setIndicatorDrawable(R.drawable.tab_s_indicator)
            tabItem.setIndicatorSize(15, 2)
            tabItem.setTitleIndictorGap(5)
        }
        return tabItem
    }

    /**
     * 设置 Tab 数量变化监听器
     * @param index Tab 索引
     * @param fragment 实现了 TabCountListener 接口的 Fragment
     */
    private fun setupTabCountListener(index: Int, fragment: TabCountListener) {
        fragment.setOnCountChangeListener { count ->
            updateTabTitle(index, count)
        }
        // 立即获取当前数量并更新
        fragment.getCurrentCount()?.let { count ->
            updateTabTitle(index, count)
        }
    }

    /**
     * 更新 Tab 标题，在原始标题后添加数量
     * @param index Tab 索引
     * @param count 数量
     */
    private fun updateTabTitle(index: Int, count: Int) {
        if (index < 0 || index >= tabList.size) {
            return
        }
        val tabItem = tabList[index]
        val originalTitle = tabOriginalTitles[index] ?: tabItem.getSubTitle()
        // 格式：原始标题 + 数量，例如："动态(10)"
        val titleWithCount = if (count > 0) {
            "$originalTitle$count"
        } else {
            originalTitle
        }
        tabItem.setSubTitle(titleWithCount)
    }

    class ContentPageAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle,
        private val data: List<MonikaBaseFragment>
    ) :
        FragmentStateAdapter(fm, lifecycle) {


        override fun createFragment(position: Int): Fragment {
            return data[position]
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}