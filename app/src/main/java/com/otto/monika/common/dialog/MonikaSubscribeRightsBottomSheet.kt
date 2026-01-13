package com.otto.monika.common.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.otto.monika.R
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.dialog.adapter.SubscribeRightsAdapter
import com.otto.monika.common.dialog.model.SubscribeRights

class MonikaSubscribeRightsBottomSheet : BottomSheetDialogFragment() {
    private var subscribeRights: List<SubscribeRights>? = null
    private var rightsAdapter: SubscribeRightsAdapter? = null

    var onRightsAddClickListener: ((String) -> Unit)? = null // 添加权益回调，参数是权益标题

    companion object {
        private const val ARG_RIGHTS_SHEET_DATA = "arg_rights_sheet_data"

        /**
         * 创建通用BottomSheet实例
         */
        fun newInstance(data: List<SubscribeRights>): MonikaSubscribeRightsBottomSheet {
            return MonikaSubscribeRightsBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableArray(ARG_RIGHTS_SHEET_DATA, data.toTypedArray())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val parcelableArray = it.getParcelableArray(ARG_RIGHTS_SHEET_DATA)
            subscribeRights = parcelableArray?.mapNotNull { parcelable ->
                parcelable as? SubscribeRights
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_rights_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    private fun initViews(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.tv_add_rights_content)
        val closeButton: View = view.findViewById(R.id.iv_add_rights_close)

        // 初始化 RecyclerView
        rightsAdapter = SubscribeRightsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = rightsAdapter
        recyclerView.addItemDecoration(VerticalSpacingItemDecoration(10))
        // 设置数据
        subscribeRights?.let {
            rightsAdapter?.setData(it)
        }

        // 设置添加按钮点击监听
        rightsAdapter?.onAddClickListener = { right ->
            // 将权益标题传回给外部
            onRightsAddClickListener?.invoke(right.rightsTitle ?: "")
            // 关闭 BottomSheet
            dismiss()
        }

        // 关闭按钮
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // 禁止拖拽并设置高度适配内容
        dialog?.let {
            val bottomSheetDialog = it as? BottomSheetDialog
            // 允许点击外部关闭
            bottomSheetDialog?.setCanceledOnTouchOutside(false)
            //bottomSheetDialog?.setCancelable(true)
            // 设置窗口背景为透明
            bottomSheetDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            // 找到 BottomSheet 的根视图并去除白色背景
            val bottomSheet =
                bottomSheetDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
}