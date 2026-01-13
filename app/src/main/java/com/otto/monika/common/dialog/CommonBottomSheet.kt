package com.otto.monika.common.dialog

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.otto.monika.R
import com.otto.monika.common.dialog.adapter.CommonBottomSheetAdapter
import com.otto.monika.common.dialog.model.CommonBottomSheetData
import com.otto.monika.common.dialog.views.MaxHeightRecycleView
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.common.views.MonikaCustomButton

/**
 * 通用BottomSheet DialogFragment
 */
class CommonBottomSheet : BottomSheetDialogFragment() {

    private lateinit var closeBtn: ImageView
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var itemRecyclerView: MaxHeightRecycleView
    private lateinit var confirmBtn: MonikaCustomButton
    private lateinit var adapter: CommonBottomSheetAdapter

    private var sheetData: CommonBottomSheetData? = null
    private var onItemChanged: ((CommonBottomSheetData?) -> Unit)? = null

    private var isMultiSelectMode: Boolean = false
    private var maxSelectCount: Int = -1

    private var maxHeight: Int = 0

    companion object {
        private const val ARG_SHEET_DATA = "arg_sheet_data"
        private const val ARG_MULTI_SELECT = "arg_multi_select"
        private const val ARG_MULTI_SELECT_COUNT = "arg_multi_select_count"
        private const val ARG_MAX_HEIGHT = "arg_max_height"

        /**
         * 创建通用BottomSheet实例（多选模式）
         */
        fun newInstance(
            data: CommonBottomSheetData,
            maxHeight: Int = 0,
            isMultiSelect: Boolean = false,
            maxSelectCount: Int = -1
        ): CommonBottomSheet {
            return CommonBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SHEET_DATA, data)
                    putBoolean(ARG_MULTI_SELECT, isMultiSelect)
                    putInt(ARG_MAX_HEIGHT, maxHeight)
                    putInt(ARG_MULTI_SELECT_COUNT, maxSelectCount)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sheetData = it.getParcelable(ARG_SHEET_DATA)
            isMultiSelectMode = it.getBoolean(ARG_MULTI_SELECT, false)
            maxSelectCount = it.getInt(ARG_MULTI_SELECT_COUNT, maxSelectCount)
            maxHeight = it.getInt(ARG_MAX_HEIGHT, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupListeners()
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

    /**
     * 设置项选择回调（单选模式）
     */
    fun setOnItemChangeListener(listener: (CommonBottomSheetData?) -> Unit) {
        this.onItemChanged = listener
    }


    private fun initViews(view: View) {
        closeBtn = view.findViewById(R.id.iv_common_sheet_close_btn)
        titleText = view.findViewById(R.id.tv_common_sheet_title_text)
        descriptionText = view.findViewById(R.id.iv_common_sheet_description_text)
        itemRecyclerView = view.findViewById(R.id.iv_common_sheet_recycler_view)
        confirmBtn = view.findViewById(R.id.iv_common_sheet_confirm_btn)
        if (maxHeight > 0) {
            itemRecyclerView.setMaxHeight(DipUtils.dpToPx(maxHeight))
        }
        sheetData?.let { data ->
            titleText.text = data.title
            descriptionText.text = data.description
        }
    }

    private fun setupRecyclerView() {
        sheetData?.let { data ->
            adapter = CommonBottomSheetAdapter(
                context = requireContext(),
                isMultiSelect = isMultiSelectMode,
                maxSelectCount = maxSelectCount
            )
            adapter.setData(data.itemList)
            itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            itemRecyclerView.adapter = adapter
            adapter.onItemSelectOverSize = {
                Toast.makeText(
                    requireActivity(),
                    "最多可以选择 5 个内容标签参与",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupListeners() {
        closeBtn.setOnClickListener {
            dismiss()
        }
        confirmBtn.setOnClickListener {
            onItemChanged?.invoke(sheetData)
            dismiss()
        }
    }

    /**
     * 将 dp 转换为 px
     */
    private fun convertDpToPx(dp: Int): Int {
        val metrics: DisplayMetrics = resources.displayMetrics
        return (dp * metrics.density).toInt()
    }
}

