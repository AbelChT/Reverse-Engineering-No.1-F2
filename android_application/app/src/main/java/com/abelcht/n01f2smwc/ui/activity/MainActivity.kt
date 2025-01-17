package com.abelcht.n01f2smwc.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.abelcht.n01f2smwc.R
import com.abelcht.n01f2smwc.broadcast.receiver.ChangeDateTimeBroadcastReceiver
import com.abelcht.n01f2smwc.broadcast.receiver.NewCallBroadcastReceiver
import com.abelcht.n01f2smwc.broadcast.receiver.NewMessageBroadcastReceiver
import com.abelcht.n01f2smwc.ui.viewmodel.DisplayDataViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val viewModel: DisplayDataViewModel by viewModels()
    private val TAG = "MainActivity"
    private val timeAndDateChangeBroadcastReceiver = ChangeDateTimeBroadcastReceiver(this)
    private val newCallBroadcastReceiver = NewCallBroadcastReceiver(this)
    private val newMessageBroadcastReceiver = NewMessageBroadcastReceiver(this)

    /**
     * Require all permissions need by the app
     */
    fun requirePermissions() {
        // Need permissions
        val permissionsArray = listOf(
            // Request bluetooth
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,

            // Request location
            Manifest.permission.ACCESS_FINE_LOCATION,

            // Request internet
            Manifest.permission.INTERNET,

            // Request incoming calls
            Manifest.permission.READ_PHONE_STATE,

            // Request incoming SMS
            Manifest.permission.RECEIVE_SMS
        )

        // Filter not granted permissions
        val notGrantedPermissionsArray = permissionsArray.filterNot {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        // Require permissions
        if (notGrantedPermissionsArray.isNotEmpty())
            ActivityCompat.requestPermissions(
                this,
                notGrantedPermissionsArray.toTypedArray(),
                1001
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(TAG, "onCreate")
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        mainActivityToolbar.setupWithNavController(navController, appBarConfiguration)

        // Require permissions
        requirePermissions()

        timeAndDateChangeBroadcastReceiver.setCallback {
            if (viewModel.onDateTimeChangeCallback != null) {
                viewModel.onDateTimeChangeCallback!!.invoke()
            }
            Log.i(TAG, "timeAndDateChangeBroadcastReceiver callback")
        }
        timeAndDateChangeBroadcastReceiver.configureAndRegisterReceiver()

        newCallBroadcastReceiver.setCallback {
            if (viewModel.onCallArriveCallback != null) {
                viewModel.onCallArriveCallback!!.invoke()
            }
            Log.i(TAG, "newCallBroadcastReceiver callback")
        }
        newCallBroadcastReceiver.configureAndRegisterReceiver()


        newMessageBroadcastReceiver.setCallback {
            if (viewModel.onMessageArriveCallback != null) {
                viewModel.onMessageArriveCallback!!.invoke()
            }
            Log.i(TAG, "newMessageBroadcastReceiver callback")
        }
        newMessageBroadcastReceiver.configureAndRegisterReceiver()

        // Require bluetooth to be enabled
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(
                applicationContext,
                resources.getString(R.string.enable_bluetooth_advice),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Check if location is enabled
        val isGPSEnabled: Boolean = try {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (e: Exception) {
            false
        }

        // Require location to be enabled
        if (!isGPSEnabled) {
            Toast.makeText(
                applicationContext,
                resources.getString(R.string.enable_location_advice),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onDestroy() {
        Log.i(TAG, "OnDestroy")
        timeAndDateChangeBroadcastReceiver.unRegisterReceiver()
        newCallBroadcastReceiver.unRegisterReceiver()
        newMessageBroadcastReceiver.unRegisterReceiver()
        super.onDestroy()
    }
}
