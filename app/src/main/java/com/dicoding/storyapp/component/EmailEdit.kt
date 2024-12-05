package com.dicoding.storyapp.component

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
//import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import android.util.Patterns

class EmailEdit : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        // Add a TextWatcher to validate email format on change
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val email = editable.toString()
                if (!isValidEmail(email)) {
                    error = "Invalid email address"  // Show error message when email is invalid
                }
            }
        })
    }

    // Basic email validation pattern
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
