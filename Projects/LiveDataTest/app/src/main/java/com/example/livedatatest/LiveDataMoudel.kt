package com.example.livedatatest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
class LiveDataMoudel : ViewModel() {
    private val _cnt = MutableLiveData<Int>(0)
    val cnt = _cnt
    fun addCnt() {
        val now : Int = _cnt.value ?: 0
        _cnt.value = now + 1
    }
    fun subCnt() {
        val now = _cnt.value ?: 0
        _cnt.value = now - 1
    }
}