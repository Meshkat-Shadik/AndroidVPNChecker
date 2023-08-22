package com.example.vpn_check_test

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.NetworkRequest.Builder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugins.GeneratedPluginRegistrant
import java.net.NetworkInterface

class MainActivity : FlutterActivity() {
    private var eventSink: EventSink? = null
    val handler = Handler(Looper.getMainLooper())
    private val networkCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {

                val isConnectedToVpn = NetworkInterface.getNetworkInterfaces().toList().any {
                    it.isUp && (it.name == "tun0" || it.name == "ppp0" || it.name == "ppp"|| it.name == "pptp"|| it.name == "tun")
                }

                Log.d("NATIVE", "onAvailable: $isConnectedToVpn , $network")
                handler.post {
                    eventSink?.success(true)
                }
            }

            override fun onLost(network: Network) {
                handler.post {
                    eventSink?.success(false)
                }
            }

        }
    } else {
        //no need to do anything
        //we are already listening to the broadcast receiver
        //when it is below lollipop
        TODO("VERSION.SDK_INT < LOLLIPOP")
    }


    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val isConnectedToVpn = NetworkInterface.getNetworkInterfaces().toList().any {
                it.isUp && (it.name == "tun0" || it.name == "ppp0" || it.name == "ppp"|| it.name == "pptp"|| it.name == "tun")
            }
            if (isConnectedToVpn) {
                // Take action when VPN connection is lost
                eventSink?.success(true)
            } else {
                // Take action when VPN connection is lost
                eventSink?.success(false)
            }
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "$CHANNEL/vpn").setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
//                    Log.d("NATIVE", "onListen")
                    eventSink = events
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Register the network callback to listen for changes in the network state
                        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val request = NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                            .build()
                        cm.registerNetworkCallback(request, networkCallback)
                    } else {
                        // Register the broadcast receiver to listen for changes in the network state
                        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                        registerReceiver(broadcastReceiver, intentFilter)
                    }
                }

                override fun onCancel(arguments: Any?) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Unregister the network callback
                        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        cm.unregisterNetworkCallback(networkCallback)
                    } else {
                        // Unregister the broadcast receiver
                        unregisterReceiver(broadcastReceiver)
                    }
                }
            }
        )
    }

    companion object {
        private const val CHANNEL = "com.example.vpn_check_test"
    }
}
