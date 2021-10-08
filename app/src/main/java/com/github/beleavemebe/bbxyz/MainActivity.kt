package com.github.beleavemebe.bbxyz

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.beleavemebe.bbxyz.databinding.ActivityMainBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.ParseException
import java.util.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }
    private val sharedPref by lazy { getPreferences(MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.dateOfBirth = getSavedDate(savedInstanceState)
        addListeners()
        refreshUI()
    }

    private fun addListeners() = with(binding) {
        dateTi.setEndIconOnClickListener { pickDateOfBirth() }
        dateEt.doAfterTextChanged { editable ->
            try {
                val input = editable.toString()
                val date = parseDate(input)
                refreshDate(date)
                log("Parsed date '$input' => $date")
            } catch (e: ParseException) {
                viewModel.dateOfBirth = null
                log(e.message)
            }
            updateButton()
        }
        getGenerationButton.setOnClickListener {
            updateGenerationInfo()
            dateEt.clearFocus()
            removeKeyboardFocusFrom(dateEt)
        }
    }

    private fun parseDate(string: String): Date =
        SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).apply {
            isLenient = false
        }.parse(string) ?: throw ParseException("Invalid date: $string", 0)


    private fun pickDateOfBirth() {
        MaterialDatePicker.Builder.datePicker()
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setSelection(viewModel.dateOfBirth?.time ?: System.currentTimeMillis())
            .setTitleText(R.string.date_of_birth_hint)
            .build().apply {
                addOnPositiveButtonClickListener { ms ->
                    val date = Date(ms)
                    refreshDate(date)
                    updateTextInput()
                }
            }.show(supportFragmentManager, "DatePicker")
    }

    private fun formatDate(date: Date?): String? = date?.let {
        DateFormat.format(DATE_PATTERN, it).toString()
    }

    private fun refreshDate(date: Date) = with(binding) {
        viewModel.dateOfBirth = date
        updateButton()
    }

    private fun refreshUI() {
        updateButton()
        updateGenerationInfo()
        updateTextInput()
    }

    private fun updateTextInput() = with(binding.dateEt) {
        val dateString = formatDate(viewModel.dateOfBirth)
        setText(dateString)
        clearFocus()
    }


    private fun updateButton() = with(binding.getGenerationButton) {
        isEnabled = viewModel.dateOfBirth != null
    }

    private fun updateGenerationInfo() = with(binding) {
        viewModel.generationStringRes?.let { stringId ->
            generationTv.text = getString(stringId)
            showGenerationInfo()
        } ?: hideGenerationInfo()
    }

    private fun hideGenerationInfo() = with(binding) {
        generationCard.isVisible = false
    }

    private fun showGenerationInfo() = with(binding) {
        generationCard.isVisible = true
        val generationPic = getGenerationImg()
        generationIv.setImageDrawable(generationPic)
    }

    private fun getGenerationImg(): Drawable? {
        val imgId = getGenerationDrawableResId()
        val drawable = AppCompatResources.getDrawable(this, imgId!!)
        return drawable
    }

    @DrawableRes
    private fun getGenerationDrawableResId(): Int? = when (viewModel.generationStringRes) {
        R.string.generation_baby_boomer -> R.drawable.bb
        R.string.generation_x -> R.drawable.x
        R.string.generation_y -> R.drawable.y
        R.string.generation_z -> R.drawable.zoomer
        R.string.generation_unknown -> R.drawable.unknown
        else -> null
    }

    private fun removeKeyboardFocusFrom(view: View) {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val dateToSave = formatDate(viewModel.dateOfBirth) ?: ""
        outState.putString(KEY_DATE, dateToSave)
        log("Saving date '$dateToSave' to saved instance state")
    }

    override fun onStop() {
        super.onStop()
        saveDateToSharedPreferences()
    }

    private fun getSavedDate(savedInstanceState: Bundle?): Date? {
        val savedDate: String? =
            getDateFromSavedInstanceState(savedInstanceState) ?: getDateFromSharedPreferences()
        val date = savedDate?.let {
            try {
                parseDate(it)
            } catch (e: ParseException) {
                null
            }
        }

        log("Found '$date' date from previous sessions")
        return date
    }

    private fun getDateFromSavedInstanceState(savedInstanceState: Bundle?): String? =
        savedInstanceState?.getString(KEY_DATE, "")

    private fun getDateFromSharedPreferences(): String? =
        sharedPref.getString(KEY_DATE, "")

    private fun saveDateToSharedPreferences() = sharedPref.edit().apply {
        val dateToSave = formatDate(viewModel.dateOfBirth) ?: ""
        putString(KEY_DATE, dateToSave)
        apply()
        log("Saving date '$dateToSave' to shared preferences")
    }

    companion object {
        const val DATE_PATTERN = "dd/MM/yyyy"
        const val KEY_DATE = "bbxyz.date"

        fun log(msg: String?) = msg?.let {
            Log.d("debug", it)
        }
    }
}

