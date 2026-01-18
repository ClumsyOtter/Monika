package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.otto.monika.R
import com.otto.network.model.user.response.AccountType

/**
 * 账户头部创作者信息 View
 * 包含：左侧文字+数字 + 分割线 + 右侧文字+数字
 */
class AccountHeadCreatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val leftContainer: LinearLayout
    private val rightContainer: LinearLayout
    private val leftLabelText: TextView
    private val leftValueText: TextView
    private val rightLabelText: TextView
    private val rightValueText: TextView
    private var mAccountType: AccountType? = null
    var onLeftItemClick: ((AccountType?) -> Unit)? = null
    var onRightItemClick: ((AccountType?) -> Unit)? = null

    init {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_account_head_creator, this, true)
        leftContainer = findViewById(R.id.tv_head_creator_left_container)
        leftContainer.setOnClickListener {
            onLeftItemClick?.invoke(mAccountType)
        }
        rightContainer = findViewById(R.id.tv_head_creator_right_container)
        rightContainer.setOnClickListener {
            onRightItemClick?.invoke(mAccountType)
        }
        // 获取子视图
        leftLabelText = findViewById(R.id.tv_head_creator_left_label)
        leftValueText = findViewById(R.id.tv_head_creator_left_value)
        rightLabelText = findViewById(R.id.tv_head_creator_right_label)
        rightValueText = findViewById(R.id.tv_head_creator_right_value)
    }

    fun setAccountType(accountType: AccountType) {
        mAccountType = accountType
        when (accountType) {
            AccountType.SELF_CREATOR -> {
                leftLabelText.text = "我的收入: "
                rightLabelText.text = "我的粉丝: "
            }

            AccountType.OTHER_CREATOR -> {
                leftLabelText.text = "TA的订阅: "
                rightLabelText.text = "TA的粉丝: "
            }

            else -> {

            }
        }
    }

    fun setValue(leftValue: String?, rightValue: String?) {
        leftValueText.text = leftValue
        rightValueText.text = rightValue
    }

}

