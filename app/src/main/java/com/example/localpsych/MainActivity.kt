package com.example.localpsych

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.example.localpsych.databinding.ActivityMainBinding
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {
    val mIntentFilter = IntentFilter()
    lateinit var mChannel: WifiP2pManager.Channel
    lateinit var mManager: WifiP2pManager

    private lateinit var socket: Socket
    private lateinit var serverSocket: ServerSocket

    private var _binding:ActivityMainBinding? = null
    private val binding by lazy{
        _binding!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askPerms()

        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(this, mainLooper, null)



    }

    @SuppressLint("MissingPermission")

    fun askPerms() {
        val foregroundPermLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("taget", "fine-location")

                }

                permissions.getOrDefault(android.Manifest.permission.NEARBY_WIFI_DEVICES, false) -> {
                    Log.d("taget", "nearby devices")
                }

                else -> {
                    Log.d("taget", "no permissions")

                }
            }
        }
        foregroundPermLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.NEARBY_WIFI_DEVICES,android.Manifest.permission.ACCESS_WIFI_STATE,android.Manifest.permission.CHANGE_WIFI_STATE,))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}