package com.seachal.ocustomtablayout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.seachal.ocustomtablayout.customtab.CustomTabLayout

class MainActivity : AppCompatActivity() {
    
    // 示例数据
    private val tabTitles = listOf("月考", "期中", "期末", "高考模考", "高考真题")
    
    // ViewPager2适配器
    private lateinit var pagerAdapter: PageAdapter
    
    // 当前指示器位置 (默认底部)
    private var currentIndicatorPosition = CustomTabLayout.INDICATOR_POSITION_BOTTOM

    // TabLayout引用
    private lateinit var defaultTabLayout: CustomTabLayout
    private lateinit var customTabLayout: CustomTabLayout
    private lateinit var advancedTabLayout: CustomTabLayout
    
    // 官方TabLayout引用
    private lateinit var officialTabLayout: TabLayout
    private lateinit var officialTabLayout2: TabLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化TabLayout引用
        defaultTabLayout = findViewById(R.id.default_tab_layout)
        customTabLayout = findViewById(R.id.custom_tab_layout)
        advancedTabLayout = findViewById(R.id.advanced_tab_layout)
        officialTabLayout = findViewById(R.id.official_tab_layout)
        officialTabLayout2 = findViewById(R.id.official_tab_layout2)
        // 初始化ViewPager2
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        pagerAdapter = PageAdapter(tabTitles)
        viewPager.adapter = pagerAdapter
        
        // 设置Tab和ViewPager的联动
        setupTabs(viewPager)
        
        // 设置切换指示器位置的按钮
        setupToggleButton()
        
        // 设置官方TabLayout
        setupOfficialTabLayout(viewPager)
        setupOfficialTabLayout2(viewPager)
    }
    
    /**
     * 设置官方TabLayout
     */
    private fun setupOfficialTabLayout(viewPager: ViewPager2) {
        // 移除所有标签
        officialTabLayout.removeAllTabs()
        
        // 使用TabLayoutMediator将TabLayout与ViewPager2连接
        TabLayoutMediator(officialTabLayout, viewPager) { tab, position ->
            // 创建自定义视图
            val customView = LayoutInflater.from(this).inflate(R.layout.t_item_tab_layout, null)
            val tabTextView = customView.findViewById<TextView>(R.id.t_tv_tab)
            val tabImageView = customView.findViewById<ImageView>(R.id.t_iv_tab)
            
            // 设置文本
            tabTextView.text = tabTitles[position]
            
            // 初始状态
            val isSelected = position == viewPager.currentItem
            tabImageView.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            tabTextView.setTextColor(if (isSelected) resources.getColor(R.color.tab_selected_color) else resources.getColor(R.color.tab_unselected_color))
            
            // 设置自定义视图
            tab.customView = customView
        }.attach()
        
        // 添加选项卡选择监听器
        officialTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 显示选中标签的指示器
                tab.customView?.findViewById<ImageView>(R.id.t_iv_tab)?.visibility = View.VISIBLE
                // 修改选中项文本颜色
                tab.customView?.findViewById<TextView>(R.id.t_tv_tab)?.setTextColor(resources.getColor(R.color.tab_selected_color))
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {
                // 隐藏未选中标签的指示器
                tab.customView?.findViewById<ImageView>(R.id.t_iv_tab)?.visibility = View.INVISIBLE
                // 修改未选中项文本颜色
                tab.customView?.findViewById<TextView>(R.id.t_tv_tab)?.setTextColor(resources.getColor(R.color.tab_unselected_color))
            }
            
            override fun onTabReselected(tab: TabLayout.Tab) {
                // 重新选中标签时的操作
            }
        })
    }


    private fun setupOfficialTabLayout2(viewPager: ViewPager2) {
        // 移除所有标签
        officialTabLayout2.removeAllTabs()

        // 使用TabLayoutMediator将TabLayout与ViewPager2连接
        TabLayoutMediator(officialTabLayout2, viewPager) { tab, position ->
            // 创建自定义视图
            val customView = LayoutInflater.from(this).inflate(R.layout.t_item_tab_layout2, null)
            val tabTextView = customView.findViewById<TextView>(R.id.t_tv_tab)
            val tabImageView = customView.findViewById<ImageView>(R.id.t_iv_tab)

            // 设置文本
            tabTextView.text = tabTitles[position]

            // 初始状态
            val isSelected = position == viewPager.currentItem
            tabImageView.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            tabTextView.setTextColor(if (isSelected) resources.getColor(R.color.tab_selected_color) else resources.getColor(R.color.tab_unselected_color))

            // 设置自定义视图
            tab.customView = customView
        }.attach()

        // 添加选项卡选择监听器
        officialTabLayout2.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 显示选中标签的指示器
                tab.customView?.findViewById<ImageView>(R.id.t_iv_tab)?.visibility = View.VISIBLE
                // 修改选中项文本颜色
                tab.customView?.findViewById<TextView>(R.id.t_tv_tab)?.setTextColor(resources.getColor(R.color.tab_selected_color))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // 隐藏未选中标签的指示器
                tab.customView?.findViewById<ImageView>(R.id.t_iv_tab)?.visibility = View.INVISIBLE
                // 修改未选中项文本颜色
                tab.customView?.findViewById<TextView>(R.id.t_tv_tab)?.setTextColor(resources.getColor(R.color.tab_unselected_color))
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // 重新选中标签时的操作
            }
        })
    }

    /**
     * 设置切换指示器位置的按钮
     */
    private fun setupToggleButton() {
        val toggleButton = findViewById<Button>(R.id.toggle_indicator_position)
        toggleButton.setOnClickListener {
            // 切换指示器位置
            currentIndicatorPosition = if (currentIndicatorPosition == CustomTabLayout.INDICATOR_POSITION_BOTTOM) {
                CustomTabLayout.INDICATOR_POSITION_TOP
            } else {
                CustomTabLayout.INDICATOR_POSITION_BOTTOM
            }
            
            // 更新按钮文本
            val newText = if (currentIndicatorPosition == CustomTabLayout.INDICATOR_POSITION_BOTTOM) {
                "切换指示器到顶部"
            } else {
                "切换指示器到底部"
            }
            toggleButton.text = newText
            
            // 为所有TabLayout设置新的指示器位置
            defaultTabLayout.setIndicatorPosition(currentIndicatorPosition)
            customTabLayout.setIndicatorPosition(currentIndicatorPosition)
            advancedTabLayout.setIndicatorPosition(currentIndicatorPosition)
        }
    }
    
    /**
     * 设置Tab和ViewPager的联动
     */
    private fun setupTabs(viewPager: ViewPager2) {
        // 使用默认指示器的CustomTabLayout
        defaultTabLayout.setTabs(tabTitles)
        
        // 使用自定义弧形指示器的CustomTabLayout
        customTabLayout.setTabs(tabTitles)
        
        // 使用高级自定义指示器的CustomTabLayout
        advancedTabLayout.setTabs(tabTitles)
        
        // 设置ViewPager页面变化监听
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // 更新所有TabLayout的选中状态
                defaultTabLayout.selectTab(position)
                customTabLayout.selectTab(position)
                advancedTabLayout.selectTab(position)
            }
        })
        
        // 设置默认Tab选中监听
        defaultTabLayout.setOnTabSelectedListener(object : CustomTabLayout.OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                // 更新ViewPager页面
                viewPager.currentItem = position
                // 同步其他TabLayout
                customTabLayout.selectTab(position, false)
                advancedTabLayout.selectTab(position, false)
            }
        })
        
        // 设置自定义Tab选中监听
        customTabLayout.setOnTabSelectedListener(object : CustomTabLayout.OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                // 更新ViewPager页面
                viewPager.currentItem = position
                // 同步其他TabLayout
                defaultTabLayout.selectTab(position, false)
                advancedTabLayout.selectTab(position, false)
            }
        })
        
        // 设置高级Tab选中监听
        advancedTabLayout.setOnTabSelectedListener(object : CustomTabLayout.OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                // 更新ViewPager页面
                viewPager.currentItem = position
                // 同步其他TabLayout
                defaultTabLayout.selectTab(position, false)
                customTabLayout.selectTab(position, false)
            }
        })
    }
    
    /**
     * ViewPager2适配器
     */
    private inner class PageAdapter(private val items: List<String>) : 
            RecyclerView.Adapter<PageViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_page, parent, false)
            return PageViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
    }
    
    /**
     * 页面ViewHolder
     */
    private class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.text_view)
        
        fun bind(text: String) {
            textView.text = "这是「${text}」页面内容"
        }
    }
}