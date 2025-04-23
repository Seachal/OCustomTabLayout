package com.seachal.ocustomtablayout.customtab

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seachal.ocustomtablayout.R

/**
 * 自定义TabLayout，基于RecyclerView实现
 * 支持自定义指示器，通过传入一个包含ImageView的布局文件
 */
class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // 指示器位置常量
    companion object {
        const val INDICATOR_POSITION_BOTTOM = 0
        const val INDICATOR_POSITION_TOP = 1
    }

    // RecyclerView用于水平排列各个Tab项
    private val recyclerView = RecyclerView(context)
    
    // 适配器
    private val adapter = TabAdapter()
    
    // 指示器布局资源ID，默认为0（未设置）
    private var indicatorLayoutResId = 0
    
    // 指示器ImageView的ID
    private var indicatorImageViewId = 0
    
    // 当前选中的位置
    private var selectedPosition = 0
    
    // Tab项数据列表
    private val tabItems = mutableListOf<String>()
    
    // Tab选中监听器
    private var onTabSelectedListener: OnTabSelectedListener? = null
    
    // 默认选中文本颜色
    private var selectedTextColor = 0xFF000000.toInt() // 默认黑色
    
    // 默认未选中文本颜色
    private var unselectedTextColor = 0xFF666666.toInt() // 默认灰色
    
    // 文本大小（单位：SP）
    private var textSize = 14f
    
    // Tab项间距
    private var tabItemSpacing = 0
    
    // 指示器位置，默认在底部
    private var indicatorPosition = INDICATOR_POSITION_BOTTOM
    
    // 指示器边距
    private var indicatorMargin = 0
    
    init {
        // 设置为垂直布局
        orientation = VERTICAL
        
        // 解析自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout)
        try {
            indicatorLayoutResId = typedArray.getResourceId(R.styleable.CustomTabLayout_indicatorLayout, 0)
            indicatorImageViewId = typedArray.getResourceId(R.styleable.CustomTabLayout_indicatorImageViewId, 0)
            selectedTextColor = typedArray.getColor(R.styleable.CustomTabLayout_selectedTextColor, selectedTextColor)
            unselectedTextColor = typedArray.getColor(R.styleable.CustomTabLayout_unselectedTextColor, unselectedTextColor)
            textSize = typedArray.getDimension(R.styleable.CustomTabLayout_tabTextSize, 
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, resources.displayMetrics))
            tabItemSpacing = typedArray.getDimensionPixelSize(R.styleable.CustomTabLayout_tabItemSpacing, 0)
            indicatorPosition = typedArray.getInt(R.styleable.CustomTabLayout_indicatorPosition, INDICATOR_POSITION_BOTTOM)
            indicatorMargin = typedArray.getDimensionPixelSize(R.styleable.CustomTabLayout_indicatorMargin, 
                resources.getDimensionPixelSize(R.dimen.indicator_margin))
        } finally {
            typedArray.recycle()
        }
        
        // 设置RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null // 禁用动画以提高性能
        recyclerView.setHasFixedSize(true) // 如果Tab大小固定，可以提高性能
        
        if (tabItemSpacing > 0) {
            recyclerView.addItemDecoration(TabItemDecoration(tabItemSpacing))
        }
        
        // 设置硬件加速以提高渲染性能
        setLayerType(LAYER_TYPE_HARDWARE, null)
        recyclerView.setLayerType(LAYER_TYPE_HARDWARE, null)
        
        // 添加RecyclerView到布局
        addView(recyclerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }
    
    /**
     * 设置Tab数据
     * @param tabs Tab标题列表
     */
    fun setTabs(tabs: List<String>) {
        tabItems.clear()
        tabItems.addAll(tabs)
        adapter.notifyDataSetChanged()
        
        // 默认选中第一项
        if (tabItems.isNotEmpty() && selectedPosition == 0) {
            selectTab(0, false)
        }
    }
    
    /**
     * 选中指定位置的Tab
     * @param position 要选中的Tab位置
     * @param smoothScroll 是否使用平滑滚动效果
     */
    fun selectTab(position: Int, smoothScroll: Boolean = true) {
        if (position >= 0 && position < tabItems.size && position != selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = position
            
            // 确保目标Tab可见
            if (smoothScroll) {
                recyclerView.smoothScrollToPosition(position)
            } else {
                recyclerView.scrollToPosition(position)
            }
            
            // 更新Tab项UI - 只需要更新变化的项
            adapter.notifyItemChanged(previousPosition)
            adapter.notifyItemChanged(selectedPosition)
            
            // 回调监听器
            onTabSelectedListener?.onTabSelected(selectedPosition)
        }
    }
    
    /**
     * 设置Tab选中监听器
     */
    fun setOnTabSelectedListener(listener: OnTabSelectedListener) {
        onTabSelectedListener = listener
    }
    
    /**
     * 设置指示器布局
     * @param layoutResId 指示器布局资源ID
     * @param imageViewId 指示器ImageView的ID
     */
    fun setIndicatorLayout(layoutResId: Int, imageViewId: Int) {
        if (layoutResId == 0) return
        
        indicatorLayoutResId = layoutResId
        indicatorImageViewId = imageViewId
        
        // 更新所有Tab项
        adapter.notifyDataSetChanged()
    }
    
    /**
     * 设置指示器位置
     * @param position 位置，可选值为INDICATOR_POSITION_BOTTOM或INDICATOR_POSITION_TOP
     */
    fun setIndicatorPosition(position: Int) {
        if (position != INDICATOR_POSITION_BOTTOM && position != INDICATOR_POSITION_TOP) {
            return
        }
        
        if (indicatorPosition == position) {
            return
        }
        
        indicatorPosition = position
        
        // 更新所有Tab项
        adapter.notifyDataSetChanged()
    }
    
    /**
     * 设置文本颜色
     */
    fun setTextColors(selectedColor: Int, unselectedColor: Int) {
        selectedTextColor = selectedColor
        unselectedTextColor = unselectedColor
        adapter.notifyDataSetChanged()
    }
    
    /**
     * 设置文本大小
     */
    fun setTextSize(sizeSp: Float) {
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, resources.displayMetrics)
        adapter.notifyDataSetChanged()
    }
    
    /**
     * 设置Tab项间距
     */
    fun setTabItemSpacing(spacing: Int) {
        if (tabItemSpacing == spacing) return
        tabItemSpacing = spacing
        recyclerView.removeItemDecorations()
        if (spacing > 0) {
            recyclerView.addItemDecoration(TabItemDecoration(spacing))
        }
        adapter.notifyDataSetChanged()
    }
    
    /**
     * Tab适配器
     */
    private inner class TabAdapter : RecyclerView.Adapter<TabViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
            // 创建包含文本和指示器的Tab项布局
            val tabItemLayout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                
                // 启用硬件加速
                setLayerType(LAYER_TYPE_HARDWARE, null)
            }
            
            // 创建文本视图
            val textView = TextView(parent.context).apply {
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.tab_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.tab_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.tab_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.tab_padding_vertical)
                )
                textSize = this@CustomTabLayout.textSize / resources.displayMetrics.scaledDensity
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER
                
                // 启用硬件加速
                setLayerType(LAYER_TYPE_HARDWARE, null)
            }
            
            // 根据指示器位置，决定添加顺序
            if (indicatorPosition == INDICATOR_POSITION_TOP) {
                // 创建指示器视图
                val indicatorView = createIndicatorView(parent.context)
                // 先添加指示器，再添加文本
                tabItemLayout.addView(indicatorView)
                tabItemLayout.addView(textView)
            } else {
                // 先添加文本，再添加指示器
                tabItemLayout.addView(textView)
                val indicatorView = createIndicatorView(parent.context)
                tabItemLayout.addView(indicatorView)
            }
            
            return TabViewHolder(tabItemLayout, textView)
        }
        
        /**
         * 创建指示器视图
         */
        private fun createIndicatorView(context: Context): View {
            if (indicatorLayoutResId != 0) {
                // 使用自定义指示器布局
                return LayoutInflater.from(context).inflate(indicatorLayoutResId, null).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (indicatorPosition == INDICATOR_POSITION_TOP) {
                            bottomMargin = indicatorMargin
                        } else {
                            topMargin = indicatorMargin
                        }
                        gravity = android.view.Gravity.CENTER
                    }
                    visibility = View.INVISIBLE // 默认隐藏
                    
                    // 启用硬件加速
                    setLayerType(LAYER_TYPE_HARDWARE, null)
                }
            } else {
                // 使用默认指示器
                return LayoutInflater.from(context).inflate(R.layout.default_tab_indicator, null).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (indicatorPosition == INDICATOR_POSITION_TOP) {
                            bottomMargin = indicatorMargin
                        } else {
                            topMargin = indicatorMargin
                        }
                        gravity = android.view.Gravity.CENTER
                    }
                    visibility = View.INVISIBLE // 默认隐藏
                    
                    // 启用硬件加速
                    setLayerType(LAYER_TYPE_HARDWARE, null)
                }
            }
        }
        
        override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
            // 设置文本
            holder.textView.text = tabItems[position]
            
            // 设置文本颜色
            holder.textView.setTextColor(if (position == selectedPosition) selectedTextColor else unselectedTextColor)
            
            // 设置指示器可见性
            val isSelected = position == selectedPosition
            holder.getIndicatorView()?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            
            // 点击事件
            holder.itemView.setOnClickListener {
                selectTab(position)
            }
        }
        
        override fun getItemCount(): Int = tabItems.size
    }
    
    /**
     * Tab视图持有者
     */
    private inner class TabViewHolder(itemView: View, val textView: TextView) : RecyclerView.ViewHolder(itemView) {
        /**
         * 获取指示器视图
         */
        fun getIndicatorView(): View? {
            val container = itemView as LinearLayout
            val indicatorIndex = if (indicatorPosition == INDICATOR_POSITION_TOP) 0 else 1
            return if (container.childCount > indicatorIndex) container.getChildAt(indicatorIndex) else null
        }
    }
    
    /**
     * Tab项间距装饰器
     */
    private class TabItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.right = spacing
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = spacing
            }
        }
    }
    
    /**
     * Tab选中监听器接口
     */
    interface OnTabSelectedListener {
        fun onTabSelected(position: Int)
    }
    
    /**
     * 扩展函数：移除RecyclerView所有的ItemDecoration
     */
    private fun RecyclerView.removeItemDecorations() {
        while (itemDecorationCount > 0) {
            removeItemDecorationAt(0)
        }
    }
} 