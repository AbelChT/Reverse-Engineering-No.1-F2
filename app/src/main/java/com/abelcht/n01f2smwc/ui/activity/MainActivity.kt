package com.abelcht.n01f2smwc.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.abelcht.n01f2smwc.R
import com.abelcht.n01f2smwc.broadcast.receiver.ChangeDateTimeBroadcastReceiver
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // private val viewModel: DisplayDataViewModel by viewModels()
    private val TAG = "MainActivity"
    private val timeAndDateChangeBroadcastReceiver = ChangeDateTimeBroadcastReceiver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(TAG, "onCreate")
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        mainActivityToolbar.setupWithNavController(navController, appBarConfiguration)

        timeAndDateChangeBroadcastReceiver.setCallback {
//            if (viewModel.onDateTimeChangeCallback != null) {
//                viewModel.onDateTimeChangeCallback!!.invoke()
//            }
            Log.i(TAG, "timeAndDateChangeBroadcastReceiver callback")
        }
        timeAndDateChangeBroadcastReceiver.configureAndRegisterReceiver()

        //TODO: Register CALL and MESSAGE
    }

    override fun onDestroy() {
        Log.i(TAG, "OnDestroy")
        timeAndDateChangeBroadcastReceiver.unRegisterReceiver()
        super.onDestroy()
    }
}
