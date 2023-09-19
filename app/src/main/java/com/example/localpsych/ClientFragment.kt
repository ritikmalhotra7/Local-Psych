package com.example.localpsych

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localpsych.databinding.FragmentClientBinding
import com.example.localpsych.databinding.FragmentHomeBinding


class ClientFragment : Fragment() {
    private var _binding: FragmentClientBinding? = null
    private val binding by lazy {
        _binding!!
    }
    private val mManager by lazy{
        (requireActivity() as MainActivity).mManager
    }
    private val mChannel by lazy{
        (requireActivity() as MainActivity).mChannel
    }
    private val mIntentFilter = IntentFilter()
    private val askPerms by lazy{
        (requireActivity() as MainActivity).askPerms()
    }
    private lateinit var peerAdapter: PeerAdapter
    private var peers: ArrayList<WifiP2pDevice> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientBinding.inflate(inflater)
        // Indicates a change in the Wi-Fi Direct status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        setViews()
        return binding.root
    }

    private fun setViews() {
        binding.apply {
            peerAdapter = PeerAdapter()
            fragmentClientRvPeers.apply {
                adapter = peerAdapter
                layoutManager = LinearLayoutManager(this@ClientFragment.requireContext())
            }
            if (ActivityCompat.checkSelfPermission(
                    this@ClientFragment.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@ClientFragment.requireContext(),
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mManager.discoverPeers(mChannel,object: WifiP2pManager.ActionListener{
                override fun onSuccess() {
                    fragmentClientTvStatus.text = "Discovery Enabled"
                }

                override fun onFailure(p0: Int) {
                    fragmentClientTvStatus.text = "Discovery Failed"
                }
            })
        }
    }
    @SuppressLint("MissingPermission")
    val peerListener = WifiP2pManager.PeerListListener { peerList ->
        var deviceNameArray = arrayOf<String>()
        var deviceArray = arrayOf<WifiP2pDevice>()
        if(!peerList.deviceList.equals(peers)){
            peers.clear()
            peers.addAll(peerList.deviceList)
            deviceNameArray = peerList.deviceList.map { it.deviceName }.toTypedArray()
            deviceArray = peerList.deviceList.toTypedArray()

            peerAdapter.apply{
                setList(deviceNameArray.toList())
                setClickListener{ item, position ->
                    val deviceInfo = deviceArray[position]
                    val config = WifiP2pConfig()
                    config.deviceAddress = deviceInfo.deviceAddress
                    mManager.connect(mChannel,config,object:WifiP2pManager.ActionListener{
                        override fun onSuccess() {
                            Log.d("taget",deviceInfo.toString())
                            binding.fragmentClientTvStatus.text = "Connection Success"
                        }

                        override fun onFailure(p0: Int) {
                            Log.d("taget-error",deviceInfo.toString())
                            Log.d("taget-error",p0.toString())
                            binding.fragmentClientTvStatus.text = "Connection Failed"
                        }
                    })
                }
            }
        }
    }
    private val wifiDirectBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(p0: Context?, intent: Intent) {
            Log.d("taget","Entered")
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    val isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    Toast.makeText(context, isWifiP2pEnabled.toString(), Toast.LENGTH_SHORT).show()
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    mManager.requestPeers(mChannel, peerListener)
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(wifiDirectBroadcastReceiver, mIntentFilter)
    }

    public override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(wifiDirectBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}