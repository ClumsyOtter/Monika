package com.otto.monika.home.fragment.mine.model

import com.otto.monika.account.rank.fragment.RankSource
import com.otto.monika.account.rank.fragment.UserSubscribeRankFragment
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.home.fragment.artwork.UserArtworkListFragment
import com.otto.monika.home.fragment.favorite.UserFavoriteListFragment
import com.otto.monika.home.fragment.favorite.creator.UserFavoriteCreatorListFragment
import com.otto.monika.home.fragment.post.PostSource
import com.otto.monika.home.fragment.post.UserPostListFragment
import com.otto.monika.home.fragment.subscribe.UserSubscribeListFragment
import com.otto.network.model.user.response.AccountType

/**
 * Tab 配置数据类
 * 定义 Tab 标题和对应的 Fragment
 */
data class TabConfig(
    val title: String,
    val fragmentFactory: () -> MonikaBaseFragment
)

/**
 * Tab 配置管理器
 */
object TabConfigManager {

    /**
     * 根据用户类型生成 Tab 配置列表
     * @param accountType 用户类型
     * @param uid 用户ID
     * @return Tab 配置列表，如果返回空列表表示不显示任何 Tab
     */
    fun generateMineTabConfigs(
        accountType: AccountType,
        uid: String,
        isOwner: Boolean
    ): List<TabConfig> {
        return when (accountType) {
            // 本人但不是创作者：显示"订阅"、"收藏"两个 Tab
            AccountType.SELF_NON_CREATOR -> {
                listOf(
                    TabConfig("订阅") { UserSubscribeListFragment.newInstance(uid) },
                    TabConfig("收藏") { UserFavoriteListFragment.newInstance(uid) }
                )
            }
            // 本人是创作者：显示"动态"、"作品"、"订阅"、"收藏"四个 Tab
            AccountType.SELF_CREATOR -> {
                listOf(
                    TabConfig("动态") {
                        UserPostListFragment.newInstance(
                            uid,
                            isOwner,
                            PostSource.User
                        )
                    },
                    TabConfig("作品") { UserArtworkListFragment.newInstance(uid) },
                    TabConfig("订阅") { UserSubscribeListFragment.newInstance(uid) },
                    TabConfig("收藏") { UserFavoriteListFragment.newInstance(uid) }
                )
            }
            // 不是本人是创作者：显示"动态"、"作品"、"订阅榜"三个 Tab
            AccountType.OTHER_CREATOR -> {
                listOf(
                    TabConfig("动态") {
                        UserPostListFragment.newInstance(
                            uid,
                            isOwner,
                            PostSource.User
                        )
                    },
                    TabConfig("作品") { UserArtworkListFragment.newInstance(uid) },
                    TabConfig("订阅榜") {
                        UserSubscribeRankFragment.newInstance(
                            RankSource.USER,
                            uid
                        )
                    }
                )
            }
            // 不是本人不是创作者：不显示任何 Tab
            AccountType.OTHER_NON_CREATOR -> {
                emptyList()
            }
        }
    }

    fun generateCollectionTabConfigs(uid: String): List<TabConfig> {
        return listOf(
            TabConfig("动态") { UserPostListFragment.newInstance(uid, false, PostSource.Favorite) },
            TabConfig("创作者") { UserFavoriteCreatorListFragment.newInstance(uid) },
        )
    }
}

