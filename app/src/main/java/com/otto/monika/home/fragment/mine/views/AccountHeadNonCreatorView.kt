package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.otto.monika.R
import com.otto.network.model.user.response.AccountType
import com.otto.network.model.user.response.AuditStatus

/**
 * 账户头部创作者申请 View
 * 包含：图片 icon、文字、数字、箭头
 */
class AccountHeadNonCreatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    private val iconImage: ImageView
    private val textImage: ImageView
    private val auditStatusText: TextView
    private val countText: TextView
    private val arrowImage: ImageView

    var onItemClickListener: ((AccountType?) -> Unit)? = null

    private var mAccountType: AccountType? = null
    private var mAuditStatus: AuditStatus = AuditStatus.NONE


    init {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_account_head_non_creator, this, true)

        // 获取子视图
        iconImage = findViewById(R.id.iv_noncreator_icon)
        textImage = findViewById(R.id.tv_noncreator_title)
        auditStatusText = findViewById(R.id.tv_audit_status)
        countText = findViewById(R.id.tv_creator_count)
        arrowImage = findViewById(R.id.iv_creator_arrow)

        // 设置点击监听
        setOnClickListener { onItemClickListener?.invoke(mAccountType) }
    }


    fun setAccountType(accountType: AccountType) {
        mAccountType = accountType
        when (accountType) {
            AccountType.SELF_NON_CREATOR -> {
                countText.isVisible = false
                iconImage.setImageResource(R.drawable.monika_empty_icon)
                textImage.setImageResource(R.drawable.monika_account_apply_creator_icon)
            }

            AccountType.OTHER_NON_CREATOR -> {
                countText.isVisible = true
                iconImage.setImageResource(R.drawable.monika_add_subscribe_title_icon)
                textImage.setImageResource(R.drawable.monika_home_cell_title_wddy)
            }

            else -> {

            }
        }
    }

    /**
     * 设置数字
     */
    fun setCount(count: String?) {
        if (count.isNullOrEmpty()) {
            countText.visibility = GONE
        } else {
            countText.visibility = VISIBLE
            countText.text = count
        }
    }

    /**
     * 设置数字（Int 类型）
     */
    fun setCount(count: Int) {
        if (count <= 0) {
            countText.visibility = GONE
        } else {
            countText.visibility = VISIBLE
            countText.text = count.toString()
        }
    }


    /**
     * 获取数字
     */
    fun getCount(): String {
        return countText.text.toString()
    }

    /**
     * 设置审核状态
     * @param status 审核状态：审核中或审核失败
     */
    fun setAuditStatus(status: AuditStatus) {
        mAuditStatus = status
        when (status) {
            AuditStatus.REVIEWING -> {
                // 审核中：显示"审核中"，灰色背景
                auditStatusText.isVisible = true
                auditStatusText.text = "审核中"
                auditStatusText.setTextColor(ContextCompat.getColor(context, R.color.text_808080))
                auditStatusText.background =
                    ContextCompat.getDrawable(context, R.drawable.monika_tag_gray_bg)
            }

            AuditStatus.FAILED -> {
                // 审核失败：显示"审核失败"，红色字体，红色背景
                auditStatusText.isVisible = true
                auditStatusText.text = "未通过"
                auditStatusText.setTextColor(ContextCompat.getColor(context, R.color.font_red))
                // 通过 tint 设置背景颜色为 color_FFE9E7
                val drawable = ContextCompat.getDrawable(context, R.drawable.monika_tag_gray_bg)
                drawable?.let {
                    val tintedDrawable = DrawableCompat.wrap(it.mutate())
                    DrawableCompat.setTint(
                        tintedDrawable,
                        ContextCompat.getColor(context, R.color.color_FFE9E7)
                    )
                    auditStatusText.background = tintedDrawable
                }
            }

            AuditStatus.NONE, AuditStatus.SUCCESS -> {
                // 无审核状态：隐藏审核状态 TextView
                auditStatusText.isVisible = false
                auditStatusText.background = null
            }
        }
    }

    /**
     * 获取审核状态
     */
    fun getAuditStatus(): AuditStatus {
        return mAuditStatus
    }
}

