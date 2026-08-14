package com.example.viewmodelsavedatatest

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.databinding.Observable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle

class MyViewModel(val app : Application, val handle: SavedStateHandle) : AndroidViewModel(app) {
    companion object {
        const val DATA_KEY = "data_key"
        const val FILE_NAME = "shp_name"
    }

    private val shp: SharedPreferences =
        app.getSharedPreferences(FILE_NAME, Application.MODE_PRIVATE)

    private val _number: LiveData<Int> by lazy {
        handle.getLiveData(DATA_KEY, 0)
    }
    val number = _number
    private var observer = Observer<Int> { value ->
        autoSave(value)
    }
    init {
        load()
        _number.observeForever(observer)
    }

    private fun load() {
        handle[DATA_KEY] = shp.getInt(DATA_KEY, 0)
    }
    private fun autoSave(num : Int) {
        shp.edit {
            putInt(DATA_KEY, num)
        }
    }
    fun add(num : Int) {
        val cur = _number.value ?: 0
        handle[DATA_KEY] = cur + num
    }

    override fun onCleared() {
        super.onCleared()
        number.removeObserver(observer)
    }
}