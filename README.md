# CustomTabLayout

CustomTabLayout是一个基于RecyclerView实现的自定义Tab布局组件，支持灵活的自定义指示器。特别适合实现带有特殊形状指示器（如弧形指示器）的Tab布局。

## 设计原理

### 核心设计思想

1. **基于RecyclerView实现**：利用RecyclerView的高性能和灵活性来实现水平滚动的Tab项。
2. **可完全自定义的指示器**：通过让开发者提供包含ImageView的布局文件来实现高度自定义的指示器效果。
3. **简单易用的API**：提供简洁明了的API，使开发者能够轻松集成和使用。

### 技术实现

1. **Tab项显示**：使用RecyclerView水平排列Tab项，每个Tab项是一个TextView。
2. **指示器实现**：
   - 采用布局叠加的方式，在RecyclerView下层放置指示器View
   - 通过测量当前选中Tab项的位置，动态调整指示器位置
   - 支持开发者通过XML布局文件完全自定义指示器的外观

3. **ViewPager2集成**：提供与ViewPager2无缝集成的能力，实现Tab和页面的联动。

## 使用指南

### 1. XML布局中使用

```xml
<com.seachal.ocustomtablayout.customtab.CustomTabLayout
    android:id="@+id/custom_tab_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingTop="12dp"
    android:paddingBottom="16dp"
    android:background="@android:color/white"
    app:selectedTextColor="#FF6B00"
    app:unselectedTextColor="#333333"
    app:tabTextSize="16sp"
    app:tabItemSpacing="48dp"
    app:indicatorLayout="@layout/custom_arc_indicator"
    app:indicatorImageViewId="@id/custom_indicator_image"/>
```

### 2. 自定义属性

| 属性名 | 说明 | 默认值 |
|--------|------|--------|
| indicatorLayout | 指示器布局资源ID | 无（使用默认指示器） |
| indicatorImageViewId | 指示器布局中ImageView的ID | 无 |
| selectedTextColor | 选中项文本颜色 | 黑色(#000000) |
| unselectedTextColor | 未选中项文本颜色 | 灰色(#666666) |
| tabTextSize | Tab文本大小 | 14sp |
| tabItemSpacing | Tab项间距 | 0dp |

### 3. 创建自定义指示器

1. 创建指示器布局文件，例如 `custom_arc_indicator.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <ImageView
        android:id="@+id/custom_indicator_image"
        android:layout_width="48dp"
        android:layout_height="12dp"
        android:src="@drawable/arc_indicator"
        android:contentDescription="@null" />

</FrameLayout>
```

2. 创建指示器图像资源，例如弧形指示器 `arc_indicator.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="12dp"
    android:viewportWidth="24"
    android:viewportHeight="12">
    
    <path
        android:fillColor="#FF6B00"
        android:pathData="M0,0 Q12,12 24,0 H0 Z" />
    
</vector>
```

### 4. 代码中使用

```kotlin
// 初始化时设置Tab数据
val tabLayout = findViewById<CustomTabLayout>(R.id.custom_tab_layout)
tabLayout.setTabs(listOf("月考", "期中", "期末", "高考模考", "高考真题"))

// 与ViewPager2联动
viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
        tabLayout.selectTab(position)
    }
})

tabLayout.setOnTabSelectedListener(object : CustomTabLayout.OnTabSelectedListener {
    override fun onTabSelected(position: Int) {
        viewPager.currentItem = position
    }
})

// 动态修改指示器
tabLayout.setIndicatorLayout(R.layout.new_custom_indicator, R.id.new_indicator_image)

// 修改文本颜色
tabLayout.setTextColors(selectedColor, unselectedColor)

// 修改文本大小
tabLayout.setTextSize(16f) // 单位：sp
```

## 优点和特性

1. **高度自定义**：可以通过自定义布局实现几乎任何形状的指示器。
2. **易于集成**：简单的API设计，易于在项目中集成。
3. **性能优越**：基于RecyclerView实现，具有优秀的性能表现。
4. **灵活可扩展**：可以轻松扩展更多功能。

## 注意事项

1. 自定义指示器布局中必须包含一个ImageView，并通过`indicatorImageViewId`指定其ID。
2. 为获得最佳效果，建议指示器图像宽度适中，不要过宽。
3. Tab项间距可以通过`tabItemSpacing`属性控制。

## 示例效果

默认指示器效果：
- 简单的矩形指示器，圆角设计
- 自动跟随选中的Tab项移动

自定义弧形指示器效果：
- 弧形设计，符合特定UI需求
- 可以完全自定义颜色、大小等属性

## 许可证

此项目遵循MIT许可证。 