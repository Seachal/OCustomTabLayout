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
) : FrameLayout(context, attrs, defStyleAttr) {

    // 指示器位置常量
    companion object {
        const val INDICATOR_POSITION_BOTTOM = 0
        const val INDICATOR_POSITION_TOP = 1
    }

    // RecyclerView用于水平排列各个Tab项
    private val recyclerView = RecyclerView(context)
    
    // 适配器
    private val adapter = TabAdapter()
    
    // 指示器视图，默认为null，可由用户自定义
    private var indicatorView: View? = null
    
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
    
    init {
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
        } finally {
            typedArray.recycle()
        }
        
        // 设置RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        if (tabItemSpacing > 0) {
            recyclerView.addItemDecoration(TabItemDecoration(tabItemSpacing))
        }
        
        // 添加RecyclerView到布局
        addView(recyclerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        
        // 如果指定了指示器布局，创建指示器视图
        if (indicatorLayoutResId != 0) {
            setupIndicatorView()
        } else {
            // 使用默认指示器
            setupDefaultIndicator()
        }
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
            post { updateIndicatorPosition(0, false) }
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
            
            // 更新Tab项UI
            adapter.notifyItemChanged(previousPosition)
            adapter.notifyItemChanged(selectedPosition)
            
            // 更新指示器位置
            updateIndicatorPosition(selectedPosition, smoothScroll)
            
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
        
        // 移除旧的指示器视图
        if (indicatorView != null) {
            removeView(indicatorView)
            indicatorView = null
        }
        
        // 创建新的指示器视图
        setupIndicatorView()
        
        // 更新指示器位置
        post { updateIndicatorPosition(selectedPosition, false) }
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
        
        // 重新创建指示器视图
        if (indicatorView != null) {
            removeView(indicatorView)
            indicatorView = null
            
            if (indicatorLayoutResId != 0) {
                setupIndicatorView()
                post { updateIndicatorPosition(selectedPosition, false) }
            } else {
                setupDefaultIndicator()
            }
        }
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
     * 设置默认指示器
     */
    private fun setupDefaultIndicator() {
        indicatorLayoutResId = R.layout.default_tab_indicator
        indicatorImageViewId = R.id.default_indicator_image
        setupIndicatorView()
    }
    
    /**
     * 设置指示器视图
     */
    private fun setupIndicatorView() {
        if (indicatorLayoutResId == 0) return
        
        // 创建指示器视图
        indicatorView = LayoutInflater.from(context).inflate(indicatorLayoutResId, this, false)
        
        // 添加指示器视图到布局，根据指示器位置决定添加的索引
        val index = if (indicatorPosition == INDICATOR_POSITION_BOTTOM) 0 else 1
        addView(indicatorView, index, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        
        // 根据指示器位置设置外边距
        val layoutParams = indicatorView?.layoutParams as? MarginLayoutParams
        layoutParams?.let {
            // 如果指示器在顶部，增加上边距，清除下边距
            // 如果指示器在底部，增加下边距，清除上边距
            if (indicatorPosition == INDICATOR_POSITION_TOP) {
                it.topMargin = resources.getDimensionPixelSize(R.dimen.indicator_margin)
                it.bottomMargin = 0
            } else {
                it.topMargin = 0
                it.bottomMargin = resources.getDimensionPixelSize(R.dimen.indicator_margin)
            }
            indicatorView?.layoutParams = it
        }
        
        // 隐藏指示器，等待第一次位置更新
        indicatorView?.visibility = View.INVISIBLE
    }
    
    /**
     * 更新指示器位置
     */
    private fun updateIndicatorPosition(position: Int, smoothScroll: Boolean) {
        if (tabItems.isEmpty() || indicatorView == null) return
        
        // 确保目标Tab可见
        if (smoothScroll) {
            recyclerView.smoothScrollToPosition(position)
        } else {
            recyclerView.scrollToPosition(position)
        }
        
        // 在下一帧获取目标Tab的位置
        post {
            val targetView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView ?: return@post
            
            // 获取目标Tab在屏幕中的位置
            val targetRect = Rect()
            targetView.getGlobalVisibleRect(targetRect)
            
            // 获取当前视图在屏幕中的位置
            val thisRect = Rect()
            getGlobalVisibleRect(thisRect)
            
            // 计算指示器的中心X坐标（相对于目标Tab的中心）
            val targetCenterX = targetRect.left + targetView.width / 2 - thisRect.left
            
            // 设置指示器位置
            indicatorView?.apply {
                // 如果用户提供了指示器ImageView的ID，则使用ImageView居中
                if (indicatorImageViewId != 0) {
                    val imageView = findViewById<ImageView>(indicatorImageViewId)
                    if (imageView != null) {
                        imageView.post {
                            val indicatorLeft = targetCenterX - imageView.width / 2
                            val layoutParams = layoutParams as MarginLayoutParams
                            layoutParams.leftMargin = indicatorLeft
                            setLayoutParams(layoutParams)
                            visibility = View.VISIBLE
                        }
                    }
                } else {
                    // 否则整个指示器视图居中
                    post {
                        val indicatorLeft = targetCenterX - width / 2
                        val layoutParams = layoutParams as MarginLayoutParams
                        layoutParams.leftMargin = indicatorLeft
                        setLayoutParams(layoutParams)
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    
    /**
     * Tab适配器
     */
    private inner class TabAdapter : RecyclerView.Adapter<TabViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
            val textView = TextView(parent.context).apply {
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.tab_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.tab_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.tab_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.tab_padding_vertical)
                )
                textSize = this@CustomTabLayout.textSize / resources.displayMetrics.scaledDensity
            }
            return TabViewHolder(textView)
        }
        
        override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
            val textView = holder.itemView as TextView
            textView.text = tabItems[position]
            textView.setTextColor(if (position == selectedPosition) selectedTextColor else unselectedTextColor)
            
            textView.setOnClickListener {
                selectTab(position)
            }
        }
        
        override fun getItemCount(): Int = tabItems.size
    }
    
    /**
     * Tab视图持有者
     */
    private class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    
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