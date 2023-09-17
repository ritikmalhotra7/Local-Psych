package com.example.localpsych

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localpsych.databinding.ActivityMainBinding
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var mReceiver: BroadcastReceiver
    private val mIntentFilter = IntentFilter()

    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mManager: WifiP2pManager

    private var peers = arrayListOf<WifiP2pDevice>()
    private var deviceNameArray = arrayOf<String>()
    private var deviceArray = arrayOf<WifiP2pDevice>()

    private lateinit var socket: Socket
    private lateinit var serverSocket: ServerSocket

    private var _binding:ActivityMainBinding? = null
    private val binding by lazy{
        _binding!!
    }
    private lateinit var peerAdapter:PeerAdapter
    private lateinit var connectedGroupAdapter:PeerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissions()

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



    }

    @SuppressLint("MissingPermission")
    private fun setViews() {
        peerAdapter = PeerAdapter()
        connectedGroupAdapter = PeerAdapter()
        binding.apply {
            activityMainRvPeers.apply {
                adapter = peerAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
            activityMainRvConnectedPeers.apply {
                adapter = connectedGroupAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
            activityMainBtDiscover.setOnClickListener {
                mManager.discoverPeers(mChannel,object:WifiP2pManager.ActionListener{
                    override fun onSuccess() {
                        binding.activityMainTvStatus.text = "Discovery Started"
                    }

                    override fun onFailure(p0: Int) {
                        Log.d("taget",p0.toString())
                        binding.activityMainTvStatus.text = "Discovery Failed"
                    }
                })
            }
        }
    }

    private fun setupPermissions() {
        val foregroundPermLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("taget", "fine-location")
                    setViews()

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

    val peerListener = WifiP2pManager.PeerListListener { peerList ->
        if(!peerList.deviceList.equals(peers)){
            peers.clear()
            peers.addAll(peerList.deviceList)
            deviceNameArray = peerList.deviceList.map { it.deviceName }.toTypedArray()
            deviceArray = peerList.deviceList.toTypedArray()

            peerAdapter.apply{
                setList(deviceNameArray.toList())
                setClickListener{ item, position ->
                    val deviceInfo = deviceArray.get(position)
                    val config = WifiP2pConfig()
                    config.deviceAddress = deviceInfo.deviceAddress
                    mManager.connect(mChannel,config,object:WifiP2pManager.ActionListener{
                        override fun onSuccess() {
                            binding.activityMainTvStatus.text = "Connection Success"
                        }

                        override fun onFailure(p0: Int) {
                            binding.activityMainTvStatus.text = "Connection Failed"
                        }
                    })
                }
            }
        }
    }

    val connectInfoListener = object:WifiP2pManager.ConnectionInfoListener{
        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo?) {
            val groupOwnerAddress = wifiP2pInfo!!.groupOwnerAddress
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){

                try{
                    serverSocket = ServerSocket(8888);
                    socket = serverSocket.accept()
                    binding.activityMainTvStatus.text = "You are the Host"
                }catch(e:Exception){
                    e.printStackTrace()
                }
            }else{
                try{
                    socket.connect(InetSocketAddress(groupOwnerAddress.hostAddress,8888),500)
                    binding.activityMainTvStatus.text = "You are the Client"
                }catch(e:Exception){
                    e.printStackTrace()
                }
            }
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