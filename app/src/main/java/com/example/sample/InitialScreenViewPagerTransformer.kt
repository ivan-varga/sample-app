package com.example.sample

import android.view.View
import androidx.viewpager.widget.ViewPager

class InitialScreenViewPagerTransformer : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {

        //        page.initialScreenFragmentSubtitleTextView.apply {
        //            if (position in 0f..1f) translationX = position * position * page.width
        //            if (position in -1f..0f) translationX = -(position * position * page.width)
        //        }

        when {
            position < -1 -> // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.alpha = 0f
            position <= 1 -> { // [-1,1]
                page.alpha = 1f

                // Counteract the default slide transition
                page.translationX = page.width * -position

                //set Y position to swipe in from top
                val yPosition = position * page.height
                page.translationY = yPosition

            }
            else -> // (1,+Infinity]
                // This page is way off-screen to the right.
                page.alpha = 0f
        }
    }
}