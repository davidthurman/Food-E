package com.thurman.foode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.AuthFailureError
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    lateinit var fragmentAdapter: TabsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentAdapter = TabsPagerAdapter(supportFragmentManager)

        var viewPager = findViewById<ViewPager>(R.id.viewpager_main)
        viewPager.adapter = fragmentAdapter

        var tabsMain = findViewById<TabLayout>(R.id.tabs_main)
        tabsMain.setupWithViewPager(viewPager)

        var tab1 = tabsMain.getTabAt(0)
        tab1?.setIcon(R.drawable.star_icon)

        var tab2 = tabsMain.getTabAt(1)
        tab2?.setIcon(R.drawable.plus_icon)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragmentAdapter.updateFavorites()
    }
}
