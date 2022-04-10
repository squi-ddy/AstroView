package com.example.astroview.ui.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.astroview.R
import com.example.astroview.ui.onboarding.adapters.ViewPagerAdapter
import me.relex.circleindicator.CircleIndicator3

class OnBoardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.onboarding_main)

        val viewPager = findViewById<ViewPager2>(R.id.view_pager2)
        val indicator = findViewById<CircleIndicator3>(R.id.indicator)

        val fragmentList = arrayListOf(
            Fragment(R.layout.onboard_first),
            Fragment(R.layout.onboard_second),
            Fragment(R.layout.onboard_third),
            OnBoardingEndFragment()
        )

        viewPager.adapter = ViewPagerAdapter(this, fragmentList)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        indicator.setViewPager(viewPager)
    }
}