package com.otto.network.model.user.response

enum class AccountType {
    SELF_NON_CREATOR,      // 本人但不是创作者
    SELF_CREATOR,          // 本人是创作者
    OTHER_CREATOR,          // 不是本人是创作者
    OTHER_NON_CREATOR      // 不是本人不是创作者
}