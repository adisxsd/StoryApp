package com.dicoding.storyapp.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.view.loginregister.RegisterActivity
import com.dicoding.storyapp.view.story.MainActivity
import com.dicoding.storyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var progressBar: ProgressBar

    private val loginViewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        edEmail = findViewById(R.id.ed_login_email)
        edPassword = findViewById(R.id.ed_login_password)
        progressBar = findViewById(R.id.progressBar)

        animateButton()

        animateProgressBar()

        tvSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        btnLogin.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            loginViewModel.login(email, password).collect { result ->
                progressBar.visibility = View.GONE

                result.onSuccess { loginResponse ->
                    val token = loginResponse.loginResult?.token
                    if (token != null) {
                        loginViewModel.saveToken(token)
                        saveSession(true)
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Token is null", Toast.LENGTH_SHORT).show()
                        btnLogin.isEnabled = true
                    }
                }
                result.onFailure {
                    Toast.makeText(applicationContext, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun saveSession(isLoggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", isLoggedIn)
        editor.apply()
    }

    private fun animateButton() {
        val scaleX = ObjectAnimator.ofFloat(btnLogin, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(btnLogin, "scaleY", 1f, 1.2f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 500
        btnLogin.setOnClickListener {
            animatorSet.start()
            val email = edEmail.text.toString()
            val password = edPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun animateProgressBar() {
        val progressAnimator = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f)
        progressAnimator.duration = 800
        progressAnimator.start()
    }
}
