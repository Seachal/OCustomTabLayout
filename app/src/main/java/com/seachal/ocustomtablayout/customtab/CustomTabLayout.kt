package com.seachal.ocustomtablayout.customtab

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
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

        // 平滑动画时长
        private const val ANIMATION_DURATION = 200L
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
    
    // 指示器边距
    private var indicatorMargin = 0

    // 当前指示器位置（左侧的X坐标）
    private var currentIndicatorLeft = 0
    
    // 当前指示器右侧的X坐标
    private var currentIndicatorRight = 0
    
    // 指示器平滑移动的动画
    private var indicatorAnimator: ValueAnimator? = null

    // 是否正在滚动
    private var isScrolling = false
    
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
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null // 禁用动画以提高性能
        recyclerView.setHasFixedSize(true) // 如果Tab大小固定，可以提高性能
        if (tabItemSpacing > 0) {
            recyclerView.addItemDecoration(TabItemDecoration(tabItemSpacing))
        }
        
        // 添加RecyclerView滚动监听器
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
                // 当滚动状态变化时，确保指示器位置正确
                updateIndicatorOnScroll()
            }
            
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 当RecyclerView滚动时，立即更新指示器位置
                updateIndicatorOnScroll()
            }
        })
        
        // 设置硬件加速以提高渲染性能
        setLayerType(LAYER_TYPE_HARDWARE, null)
        recyclerView.setLayerType(LAYER_TYPE_HARDWARE, null)
        
        // 组织布局结构
        setupLayout()
    }
    
    /**
     * 组织布局结构
     */
    private fun setupLayout() {
        // 移除所有子视图
        removeAllViews()
        
        // 根据指示器位置安排布局顺序
        if (indicatorPosition == INDICATOR_POSITION_TOP) {
            // 先添加指示器，再添加RecyclerView
            if (indicatorLayoutResId != 0) {
                setupIndicatorView()
                addView(indicatorView)
            }
            addView(recyclerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        } else {
            // 先添加RecyclerView，再添加指示器
            addView(recyclerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            if (indicatorLayoutResId != 0) {
                setupIndicatorView()
                addView(indicatorView)
            } else {
                setupDefaultIndicator()
            }
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
        
        // 重新组织布局
        setupLayout()
        
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
        
        // 重新组织布局
        setupLayout()
        
        // 更新指示器位置
        post { updateIndicatorPosition(selectedPosition, false) }
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
        addView(indicatorView)
    }
    
    /**
     * 设置指示器视图
     */
    private fun setupIndicatorView() {
        if (indicatorLayoutResId == 0) return
        
        // 创建指示器视图
        indicatorView = LayoutInflater.from(context).inflate(indicatorLayoutResId, this, false)
        
        // 设置指示器边距
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        if (indicatorPosition == INDICATOR_POSITION_TOP) {
            layoutParams.setMargins(0, indicatorMargin, 0, 0)
        } else {
            layoutParams.setMargins(0, 0, 0, indicatorMargin)
        }
        indicatorView?.layoutParams = layoutParams
        
        // 启用硬件加速以提高渲染性能
        indicatorView?.setLayerType(LAYER_TYPE_HARDWARE, null)
        
        // 隐藏指示器，等待第一次位置更新
        indicatorView?.visibility = View.INVISIBLE
    }
    
    /**
     * 当RecyclerView滚动时更新指示器位置
     */
    private fun updateIndicatorOnScroll() {
        if (tabItems.isEmpty() || indicatorView == null) return
        
        // 获取当前选中的Tab
        val targetView = recyclerView.findViewHolderForAdapterPosition(selectedPosition)?.itemView ?: return
        
        // 获取该Tab在屏幕上的位置
        val targetRect = Rect()
        targetView.getGlobalVisibleRect(targetRect)
        
        // 获取RecyclerView在屏幕中的位置
        val recyclerViewRect = Rect()
        recyclerView.getGlobalVisibleRect(recyclerViewRect)
        
        // 计算指示器居中位置
        val targetCenterX = targetRect.left + targetView.width / 2 - recyclerViewRect.left
        
        // 设置指示器位置 - 立即更新，不使用动画
        indicatorView?.apply {
            // 如果用户提供了指示器ImageView的ID，则使用ImageView居中
            if (indicatorImageViewId != 0) {
                val imageView = findViewById<ImageView>(indicatorImageViewId)
                if (imageView != null) {
                    val indicatorLeft = targetCenterX - imageView.width / 2
                    val lp = layoutParams as MarginLayoutParams
                    lp.leftMargin = indicatorLeft
                    ViewCompat.setTranslationX(this, 0f) // 确保没有额外的平移
                    layoutParams = lp
                    visibility = View.VISIBLE
                }
            } else {
                // 否则整个指示器视图居中
                val indicatorLeft = targetCenterX - width / 2
                val lp = layoutParams as MarginLayoutParams
                lp.leftMargin = indicatorLeft
                ViewCompat.setTranslationX(this, 0f) // 确保没有额外的平移
                layoutParams = lp
                visibility = View.VISIBLE
            }
        }
    }
    
    /**
     * 更新指示器位置
     */
    private fun updateIndicatorPosition(position: Int, smoothScroll: Boolean) {
        if (tabItems.isEmpty() || indicatorView == null) return
        
        // 取消正在进行的动画
        indicatorAnimator?.cancel()
        
        // 确保目标Tab可见
        if (smoothScroll) {
            recyclerView.smoothScrollToPosition(position)
        } else {
            recyclerView.scrollToPosition(position)
        }
        
        // 直接更新指示器位置，不等待下一帧
        updateIndicatorOnScroll()
        
        // 如果需要，在下一帧再次更新位置（以防Tab尚未完全加载）
        post { updateIndicatorOnScroll() }
    }
    
    /**
     * 平滑移动指示器到指定位置
     */
    private fun animateIndicator(targetLeft: Int) {
        // 获取当前位置
        val lp = indicatorView?.layoutParams as? MarginLayoutParams ?: return
        val currentLeft = lp.leftMargin
        
        // 如果正在滚动，不使用动画，直接设置位置
        if (isScrolling) {
            lp.leftMargin = targetLeft
            indicatorView?.layoutParams = lp
            return
        }
        
        // 创建动画
        indicatorAnimator = ValueAnimator.ofInt(currentLeft, targetLeft).apply {
            duration = ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                lp.leftMargin = value
                indicatorView?.layoutParams = lp
            }
            start()
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
                
                // 启用硬件加速以提高渲染性能
                setLayerType(LAYER_TYPE_HARDWARE, null)
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
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 清理动画资源
        indicatorAnimator?.cancel()
        indicatorAnimator = null
    }
} 