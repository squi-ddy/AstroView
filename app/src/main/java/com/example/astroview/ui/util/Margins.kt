package com.example.astroview.ui.util

import android.view.View
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setMargins

fun ConstraintLayout.setMargin(v: View, margin: Int) {
    (v.layoutParams as ConstraintLayout.LayoutParams).setMargins(margin)
}

fun RelativeLayout.setMargin(v: View, margin: Int) {
    (v.layoutParams as RelativeLayout.LayoutParams).setMargins(margin)
}