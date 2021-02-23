package com.example.azurecallingpoc

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.azure.communication.calling.CallAgent

class MainActivity : AppCompatActivity() {

    private lateinit var callAgent: CallAgent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getAllPermissions()
        createAgent()

        val callButton: Button = findViewById(R.id.call_button)
        callButton.setOnClickListener {
            startCall()
        }
    }

    private fun getAllPermissions() {

    }

    private fun createAgent() {

    }

    private fun startCall() {

    }
}