package com.example.basicapp.api

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.IOException

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}