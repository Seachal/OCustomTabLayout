package com.seachal.ocustomtablayout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.seachal.ocustomtablayout.customtab.CustomTabLayout

class MainActivity : AppCompatActivity() {
    
    // 示例数据
    private val tabTitles = listOf("月考", "期中", "期末", "高考模考", "高考真题")
    
    // ViewPager2适配器
    private lateinit var pagerAdapter: PageAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化ViewPager2
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        pagerAdapter = PageAdapter(tabTitles)
        viewPager.adapter = pagerAdapter
        
        // 设置Tab和ViewPager的联动
        setupTabs(viewPager)
    }
    
    /**
     * 设置Tab和ViewPager的联动
     */
    private fun setupTabs(viewPager: ViewPager2) {
        // 使用默认指示器的CustomTabLayout
        val defaultTabLayout = findViewById<CustomTabLayout>(R.id.default_tab_layout)
        defaultTabLayout.setTabs(tabTitles)
        
        // 使用自定义弧形指示器的CustomTabLayout
        val customTabLayout = findViewById<CustomTabLayout>(R.id.custom_tab_layout)
        customTabLayout.setTabs(tabTitles)
        
        // 使用高级自定义指示器的CustomTabLayout
        val advancedTabLayout = findViewById<CustomTabLayout>(R.id.advanced_tab_layout)
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