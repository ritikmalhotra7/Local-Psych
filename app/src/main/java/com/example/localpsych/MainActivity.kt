package com.example.localpsych

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.localpsych.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mReceiver: BroadcastReceiver
    private val mIntentFilter = IntentFilter()
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mManager: WifiP2pManager
    private var peers = arrayListOf<WifiP2pDevice>()
    private var deviceNameArray = arrayOf<String>()
    private var deviceArray = arrayOf<WifiP2pDevice>()

    private var _binding:ActivityMainBinding? = null
    private val binding by lazy{
        _binding!!
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Indicates a change in the Wi-Fi Direct status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(this, mainLooper, null)
        setViews()

    }

    private fun setViews() {
        binding.apply {
            activityMainBtDiscover.setOnClickListener {
                mManager.discoverPeers(mChannel,object:WifiP2pManager.ActionListener{
                    override fun onSuccess() {
                        binding.activityMainTvStatus.text = "Discovery Started"
                    }

                    override fun onFailure(p0: Int) {
                        binding.activityMainTvStatus.text = "Discovery Failed"
                    }
                })
            }
        }
    }

    private val peerListener = WifiP2pManager.PeerListListener { peerList ->
        if(!peerList.deviceList.equals(peers)){
            peers.clear()
            peers.addAll(peerList.deviceList)
            deviceNameArray = peerList.deviceList.map { it.deviceName }.toTypedArray()
            deviceArray = peerList.deviceList.toTypedArray()
        }
    }

    public override fun onResume() {
        super.onResume()
        mReceiver = WifiDirectBroadcastReceiver(mManager, mChannel, this)
        registerReceiver(mReceiver, mIntentFilter)
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(mReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}