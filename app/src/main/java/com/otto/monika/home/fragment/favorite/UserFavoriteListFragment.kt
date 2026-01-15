package com.otto.monika.home.fragment.favorite

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.databinding.FragmentUserFavoriteListBinding
import com.otto.monika.home.fragment.favorite.adapter.FavoritePagerAdapter
import com.otto.monika.home.fragment.mine.listener.TabCountListener
import com.otto.monika.home.fragment.mine.listener.TabSelectListener
import com.otto.monika.home.fragment.mine.model.TabConfigManager

/**
 * 用户收藏列表 Fragment
 * 包含自定义 Tab 和 ViewPager2，展示"动态"和"创作者"两个 Tab
 */
class UserFavoriteListFragment : MonikaBaseFragment() {

    companion object {
        private const val ARG_UID = "arg_uid"

        /**
         * 创建 Fragment 实例
         * @param uid 用户ID
         */
        @JvmStatic
        fun newInstance(uid: String?): UserFavoriteListFragment {
            return UserFavoriteListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
        }
    }

    private lateinit var binding: FragmentUserFavoriteListBinding
    private var pagerAdapter: FavoritePagerAdapter? = null
    private val tabViews = mutableListOf<TextView>()
    private var currentSelectedPosition = -1

    private val fragments = mutableListOf<MonikaBaseFragment>()

    // 保存 Tab 的原始标题，用于更新时拼接数量
    private val tabOriginalTitles = mutableMapOf<Int, String>()
    private var uid: String? = null
    override fun onFinishCreateView() {
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserFavoriteListBinding.inflate(layoutInflater)
        return binding.root
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        val tabContainer = binding.tlUserFavoriteTabs
        tabContainer.orientation = LinearLayout.HORIZONTAL
        tabContainer.gravity = Gravity.START
        val collectionTabConfigs = uid?.let { TabConfigManager.generateCollectionTabConfigs(it) }
        // 创建适配器，传入 uid
        collectionTabConfigs?.forEachIndexed { index, config ->
            val tabView = createTabView(config.title, index)
            tabContainer.addView(tabView)
            tabViews.add(tabView)
            tabOriginalTitles[index] = config.title
            fragments.add(config.fragmentFactory())
        }
        pagerAdapter = FavoritePagerAdapter(this, fragments)
        // 设置 ViewPager2
        binding.vpUserFavoritePager.adapter = pagerAdapter
        binding.vpUserFavoritePager.offscreenPageLimit = 2 // 预加载两个页面

        // 动态生成 Tab
        updateTabSelection(0)

        // 监听 ViewPager2 页面变化
        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabSelection(position)
            }
        }
        binding.vpUserFavoritePager.registerOnPageChangeCallback(pageChangeCallback)

        // 为子 Fragment 设置数量变化监听器
        setupChildFragmentCountListeners()
    }

    /**
     * 为子 Fragment 设置数量变化监听器
     */
    private fun setupChildFragmentCountListeners() {
        // 延迟执行，确保 Fragment 已经创建
        binding.vpUserFavoritePager.post {
            pagerAdapter?.let { adapter ->
                // 为"动态" Fragment 设置监听器
                if (adapter.itemCount > 0) {
                    val postFragment = adapter.getItem(0)
                    if (postFragment is TabCountListener) {
                        postFragment.setOnCountChangeListener { count ->
                            updateTabCount(0, count)
                        }
                        updateTabCount(1, postFragment.getCurrentCount())
                    }
                }
                // 为"创作者" Fragment 设置监听器
                if (adapter.itemCount > 1) {
                    val creatorFragment = adapter.getItem(1)
                    if (creatorFragment is TabCountListener) {
                        creatorFragment.setOnCountChangeListener { count ->
                            updateTabCount(1, count)
                        }
                        updateTabCount(1, creatorFragment.getCurrentCount())
                    }
                }
            }
        }
    }

    private fun updateTabCount(index: Int, count: Int?) {
        tabViews.getOrNull(index)?.apply {
            val pageTitle = tabOriginalTitles[index] ?: ""
            text = "$pageTitle$count"
        }
    }

    override fun onFragmentSelected(isSelected: Boolean) {
        super.onFragmentSelected(isSelected)
        if (isSelected) {
            runCatching {
                //用户重新进入之后，向子fragment传递选中信息
                val position = binding.vpUserFavoritePager.currentItem
                fragments.forEachIndexed { index, fragment ->
                    (fragment as? TabSelectListener)?.onFragmentSelected(index == position)
                }
            }

        }
    }

    /**
     * 创建 Tab View
     */
    private fun createTabView(title: String, position: Int): TextView {
        val tabView = TextView(requireContext()).apply {
            text = title
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dimen_15dp),
                resources.getDimensionPixelSize(R.dimen.dimen_8dp),
                resources.getDimensionPixelSize(R.dimen.dimen_15dp),
                resources.getDimensionPixelSize(R.dimen.dimen_8dp)
            )
            setOnClickListener {
                binding.vpUserFavoritePager.currentItem = position
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            if (position > 0) {
                marginStart = resources.getDimensionPixelSize(R.dimen.dimen_5dp)
            }
        }
        tabView.layoutParams = layoutParams

        return tabView
    }

    /**
     * 更新 Tab 选中状态
     */
    private fun updateTabSelection(position: Int) {
        if (currentSelectedPosition == position) return

        // 取消之前的选中状态
        tabViews.getOrNull(currentSelectedPosition)?.let { previousTab ->
            previousTab.background = null
            previousTab.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_808080)
            )
            previousTab.typeface = Typeface.DEFAULT // 正常字体
        }

        // 设置新的选中状态
        tabViews.getOrNull(position)?.let { currentTab ->
            currentTab.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.monika_corner_15_gary_f7f7f7_bg
            )
            currentTab.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_000000)
            )
            currentTab.typeface = Typeface.DEFAULT_BOLD // 加粗字体
        }
        //通知fragment选中状态
        fragments.forEachIndexed { index, fragment ->
            (fragment as? TabSelectListener)?.onFragmentSelected(index == position)
        }

        currentSelectedPosition = position
    }
}
