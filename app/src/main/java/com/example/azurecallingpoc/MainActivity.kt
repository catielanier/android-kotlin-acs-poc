package com.example.azurecallingpoc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.azure.android.communication.common.CommunicationTokenCredential
import com.azure.android.communication.common.CommunicationUserIdentifier
import com.azure.communication.calling.*

class MainActivity : AppCompatActivity() {

    private lateinit var callAgent: CallAgent
    private val callClient = CallClient()
    private lateinit var callButton: Button
    private lateinit var endCallButton: Button
    private lateinit var call: Call
    private lateinit var videoView: LinearLayout
    private lateinit var statusBar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getAllPermissions()
        createAgent()

        callButton = findViewById(R.id.call_button)
        endCallButton = findViewById(R.id.end_call_button)
        statusBar = findViewById(R.id.status_bar)
        callButton.setOnClickListener {
            startCall()
        }
        endCallButton.setOnClickListener {
            endCall()
        }
    }

    private fun getAllPermissions() {
        val requiredPermissions: Array<String> = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        )

        val permissionsToAskFor: ArrayList<String> = arrayListOf()

        for (permission in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAskFor.add(permission)
            }
        }
        if (permissionsToAskFor.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToAskFor.toArray(arrayOfNulls<String>(0)),
                1
            )
        }
    }

    private fun createAgent() {
        val userToken: String = BuildConfig.ACS_KEY
        try {
            val credential = CommunicationTokenCredential(userToken)
            callAgent = CallClient().createCallAgent(applicationContext, credential).get()
        } catch (ex: Exception) {
            Toast.makeText(
                    applicationContext,
                    "Failed to create call agent.",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startCall() {
        // get callee id
        val calleeIdView: EditText = findViewById(R.id.callee_id)
        val calleeId = calleeIdView.text.toString()

        // check for user input of id, and return a visible error message if empty string/null
        if (calleeId == "") {
            Toast.makeText(
                    applicationContext,
                    "Please enter a callee id.",
                    Toast.LENGTH_SHORT
            ).show()

            return
        }

        // get devices
        val deviceManager = callClient.deviceManager.get()
        val microphone = deviceManager.microphoneList[0]
        val speaker = deviceManager.speakerList[0]
        val camera = deviceManager.cameraList[0]

        // set devices to options
        val videoStream = LocalVideoStream(camera, applicationContext)
        val videoCallOptions = VideoOptions(videoStream)
        val options = StartCallOptions()
        options.videoOptions = videoCallOptions
        deviceManager.microphone = microphone
        deviceManager.speaker = speaker

        // start the call
        call = callAgent.call(
                applicationContext,
                arrayOf(CommunicationUserIdentifier(calleeId)),
                options
        )

        // setup video preview
        val preview: Renderer = Renderer(videoStream, applicationContext)
        val uiView: View = preview.createView(RenderingOptions(ScalingMode.Fit))
        videoView = findViewById(R.id.video_view)
        videoView.addView(uiView)
        callButton.visibility = View.GONE
        endCallButton.visibility = View.VISIBLE
        setStatus("Connected")
    }

    private fun endCall() {
        call.hangup(HangupOptions())
        videoView.removeAllViews()
        endCallButton.visibility = View.GONE
        callButton.visibility = View.VISIBLE
        setStatus("Disconnected")
    }

    private fun setStatus(status: String) {
        runOnUiThread {
            statusBar.text = status
        }
    }
}