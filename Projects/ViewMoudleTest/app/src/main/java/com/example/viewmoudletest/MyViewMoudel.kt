package com.example.viewmoudletest

import androidx.lifecycle.ViewModel

class MyViewMoudel() : ViewModel() {
    private var _number = 0
    val number get() = _number
    fun add1() : Unit{
        _number += 1
    }
    fun add2() : Unit {
        _number += 2
    }
}