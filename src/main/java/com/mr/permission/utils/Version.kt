package com.mr.permission.utils

import android.os.Build

object Version {
    fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    fun isTPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    fun isLessThanN() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
}