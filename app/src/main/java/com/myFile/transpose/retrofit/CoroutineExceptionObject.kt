package com.myFile.transpose.retrofit

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

object CoroutineExceptionObject {
    val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        Log.d("코루틴 에러","$throwable")

    }
}