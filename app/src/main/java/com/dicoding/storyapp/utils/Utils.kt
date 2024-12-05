package com.dicoding.storyapp.utils

import android.content.Context
import android.text.TextUtils
import com.dicoding.storyapp.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

fun isValidEmail(email: String?): Boolean {
    return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun <T> safeList(list: List<T?>?): List<T> {
    return list?.filterNotNull() ?: emptyList()
}

fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}

fun createRequestBody(data: String): RequestBody {
    return RequestBody.create("text/plain".toMediaTypeOrNull(), data)
}
