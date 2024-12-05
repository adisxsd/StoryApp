package com.dicoding.storyapp.utils

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.dicoding.storyapp.R
import java.text.SimpleDateFormat
import java.util.*

// Fungsi ekstensi untuk format tanggal dari ISO ke format lokal
fun Context.formatDateFromIso(isoDate: String?): String {
    if (isoDate.isNullOrEmpty()) return this.getString(R.string.unknown_date)
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return try {
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        this.getString(R.string.unknown_date)
    }
}

// Fungsi ekstensi untuk format waktu lokal dari UTC
fun Context.formatTimeToLocal(utcTime: String?, outputFormat: String = "HH:mm:ss"): String {
    if (utcTime.isNullOrEmpty()) return this.getString(R.string.unknown_time)
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val outputDateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
    return try {
        val date = inputFormat.parse(utcTime)
        outputDateFormat.format(date ?: Date())
    } catch (e: Exception) {
        this.getString(R.string.unknown_time)
    }
}

// Fungsi untuk validasi email
fun isValidEmail(email: String?): Boolean {
    return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


// Fungsi untuk menghindari null pada list
fun <T> safeList(list: List<T?>?): List<T> {
    return list?.filterNotNull() ?: emptyList()
}
