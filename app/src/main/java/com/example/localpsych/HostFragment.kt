package com.example.localpsych

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localpsych.databinding.FragmentHostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class HostFragment : Fragment() {
    private var _binding: FragmentHostBinding? = null
    private val binding by lazy {
        _binding!!
    }
    private val mManager by lazy {
        (requireActivity() as MainActivity).mManager
    }
    private val mChannel by lazy {
        (requireActivity() as MainActivity).mChannel
    }
    private val mIntentFilter = IntentFilter()
    private lateinit var connectedPeerAdapter: PeerAdapter

    private lateinit var serverSocket: ServerSocket

    private lateinit var socket: Socket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private var isGroupCreated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostBinding.inflate(inflater)
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

    @SuppressLint("MissingPermission")
    private fun setViews() {
        connectedPeerAdapter = PeerAdapter()
        binding.apply {
            fragmentHostRvConnectedPeers.apply {
                adapter = connectedPeerAdapter
                layoutManager = LinearLayoutManager(this@HostFragment.requireContext())
            }
            fragmentHostBtCreateGroup.setOnClickListener {
                if(isGroupCreated){
                    Toast.makeText(requireContext(),"Group already created",Toast.LENGTH_SHORT).show()
                }else{
                    mManager.createGroup(mChannel, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            fragmentHostTvStatus.text = "Created Group - you are the host"
                            isGroupCreated = true
                        }

                        override fun onFailure(p0: Int) {
                            fragmentHostTvStatus.text = "Created not Group - you are not the host $p0"
                        }
                    })
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val connectInfoListener = WifiP2pManager.ConnectionInfoListener { wifiP2pInfo ->
        val groupOwnerAddress = wifiP2pInfo!!.groupOwnerAddress
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            try {
                binding.fragmentHostTvStatus.text = "You are the Host"
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        serverSocket = ServerSocket(8888);
                        socket = serverSocket.accept()
                        SocketHandler.socket = socket
                        inputStream = socket.getInputStream()
                        outputStream = socket.getOutputStream()
                    }
                    findNavController().navigate(R.id.action_hostFragment_to_questionFragment)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mManager.requestGroupInfo(mChannel, WifiP2pManager.GroupInfoListener {
            connectedPeerAdapter.apply {
                setList(it.clientList.map { it.deviceName })
            }
            if (it.clientList.isNotEmpty()) {
                binding.fragmentHostBtStart.visibility = View.VISIBLE
            } else {
                binding.fragmentHostBtStart.visibility = View.GONE
            }
        })
    }
    private val wifiDirectBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            Log.d("taget", "Entered")
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    val isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    Toast.makeText(context, isWifiP2pEnabled.toString(), Toast.LENGTH_SHORT).show()
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    WifiP2pManager.GroupInfoListener { Log.d("taget-group", it.toString()) }
                    val networkInfo =
                        intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    networkInfo?.let {
                        if (it.isConnected) {
                            mManager.requestConnectionInfo(
                                mChannel,
                                connectInfoListener
                            )
                        }
                    }
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(wifiDirectBroadcastReceiver, mIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(wifiDirectBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}