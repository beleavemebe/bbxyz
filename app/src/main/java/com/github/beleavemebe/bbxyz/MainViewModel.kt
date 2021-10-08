package com.github.beleavemebe.bbxyz

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import java.util.*

class MainViewModel : ViewModel() {
    var dateOfBirth: Date? = null

    val generationStringRes: Int?
        get() {
            val calendar = Calendar.getInstance().apply { time = dateOfBirth ?: return null }
            val year = calendar.get(Calendar.YEAR)
            return when (year) {
                in 1946..1964 -> R.string.generation_baby_boomer
                in 1965..1980 -> R.string.generation_x
                in 1981..1996 -> R.string.generation_y
                in 1997..2012 -> R.string.generation_z
                else -> R.string.generation_unknown
            }
        }
}
