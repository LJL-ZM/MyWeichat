package com.example.viewmoudletest

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viewmoudletest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var  binding : ActivityMainBinding
    private val vm : MyViewMoudel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        binding.textView.text = vm.number.toString()
        binding.button.setOnClickListener {
            vm.add1()
            binding.textView.text = vm.number.toString()
        }
        binding.button3.setOnClickListener {
            vm.add2()
            binding.textView.text = vm.number.toString()
        }
    }
}