package com.example.astroview.ui.onboarding.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter (fragmentActivity: FragmentActivity,
                        private val fragments:ArrayList<Fragment>): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}