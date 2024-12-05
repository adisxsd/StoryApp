package com.dicoding.storyapp.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R

class PasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var isPasswordVisible = false
    private val visibilityIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.baseline_visibility_24)
    private val visibilityOffIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.baseline_visibility_off_24)

    init {
        setup()
        addValidationListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setup() {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        isFocusable = true
        isFocusableInTouchMode = true
        isCursorVisible = true

        // Set ikon awal
        setCompoundDrawablesWithIntrinsicBounds(null, null, visibilityOffIcon, null)

        // Toggle password visibility saat ikon diklik
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Fokus untuk menampilkan keyboard
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        setCompoundDrawablesWithIntrinsicBounds(
            null, null,
            if (isPasswordVisible) visibilityIcon else visibilityOffIcon,
            null
        )
        setSelection(text?.length ?: 0)
    }

    private fun showKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun addValidationListener() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Validasi panjang password (minimal 8 karakter)
                val input = s?.toString() ?: ""
                if (input.length < 8) {
                    error = context.getString(R.string.password_too_short)
                } else {
                    error = null
                }
            }
        })
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus) {
            showKeyboard()
        }
    }
}
