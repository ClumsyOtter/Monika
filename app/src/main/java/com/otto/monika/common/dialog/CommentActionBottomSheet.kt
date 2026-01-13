package com.otto.monika.common.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.otto.monika.R
import com.otto.monika.common.dialog.model.CommonActionGroup
import com.otto.monika.common.dialog.model.CommonActionItem

/**
 * 通用操作 BottomSheet DialogFragment
 * 根据 CommonActionGroup 动态生成操作选项
 */
class CommentActionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var titleText: TextView
    private lateinit var groupsContainer: LinearLayout
    private lateinit var cancelText: TextView

    private var actionGroups: List<CommonActionGroup> = emptyList()
    private var title: String = "操作"

    // 回调接口
    var onActionItemClickListener: ((CommonActionItem) -> Unit)? = null

    companion object {
        private const val ARG_ACTION_GROUPS = "arg_action_groups"
        private const val ARG_TITLE = "arg_title"

        /**
         * 创建操作 BottomSheet 实例
         * @param actionGroups 操作组列表
         * @param title 标题，默认为"操作"
         */
        fun newInstance(
            actionGroups: List<CommonActionGroup>,
            title: String = "操作"
        ): CommentActionBottomSheet {
            return CommentActionBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_ACTION_GROUPS, ArrayList(actionGroups))
                    putString(ARG_TITLE, title)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            val groupsList = it.getParcelableArrayList<CommonActionGroup>(ARG_ACTION_GROUPS)
            actionGroups = groupsList?.toList() ?: emptyList()
            title = it.getString(ARG_TITLE) ?: "操作"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_comment_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupContent()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheetDialog = it as? BottomSheetDialog
            // 允许点击外部关闭
            bottomSheetDialog?.setCanceledOnTouchOutside(true)
            bottomSheetDialog?.setCancelable(true)
            // 设置窗口背景为透明
            bottomSheetDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            // 找到 BottomSheet 的根视图并去除白色背景
            val bottomSheet = bottomSheetDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
            bottomSheetDialog?.behavior?.let { behavior ->
                // 禁止拖拽
                behavior.isDraggable = false
                // 设置状态为展开
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                // 设置跳过折叠状态，直接展开
                behavior.skipCollapsed = true
            }
        }
    }

    private fun initViews(view: View) {
        titleText = view.findViewById(R.id.tv_comment_action_title)
        groupsContainer = view.findViewById(R.id.ll_action_groups_container)
        cancelText = view.findViewById(R.id.tv_comment_action_cancel)
    }

    /**
     * 根据 actionGroups 动态生成内容
     */
    private fun setupContent() {
        titleText.text = title

        // 清空容器
        groupsContainer.removeAllViews()

        // 遍历每个操作组
        actionGroups.forEachIndexed { groupIndex, group ->
            // 创建组容器
            val groupLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = resources.getDimensionPixelSize(R.dimen.dimen_15dp)
                    val marginBottom = if (groupIndex < actionGroups.size - 1) {
                        resources.getDimensionPixelSize(R.dimen.dimen_10dp)
                    } else {
                        resources.getDimensionPixelSize(R.dimen.dimen_15dp)
                    }
                    setMargins(margin, 0, margin, marginBottom)
                }
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(context, R.drawable.bg_gray_round_recycler)
            }

            // 遍历组内的每个操作项
            group.items.forEach { actionItem ->
                val itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_action_option, groupLayout, false)

                val iconView: ImageView = itemView.findViewById(R.id.iv_action_option_icon)
                val contentView: TextView = itemView.findViewById(R.id.tv_action_option_content)

                // 设置图标
                if (actionItem.icon != null) {
                    iconView.visibility = View.VISIBLE
                    iconView.setImageResource(actionItem.icon)
                } else {
                    iconView.visibility = View.GONE
                }

                // 设置内容
                contentView.text = actionItem.content

                // 设置点击事件
                itemView.setOnClickListener {
                    onActionItemClickListener?.invoke(actionItem)
                    dismiss()
                }

                groupLayout.addView(itemView)
            }

            groupsContainer.addView(groupLayout)
        }
    }

    private fun setupListeners() {
        cancelText.setOnClickListener {
            dismiss()
        }
    }
}
