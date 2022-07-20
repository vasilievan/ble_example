package com.kauri_iot.ble_example.view

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kauri_iot.ble_example.R
import com.kauri_iot.ble_example.model.ExampleBluetooth
import com.kauri_iot.ble_example.ui.theme.Ble_exampleTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ViewModel by viewModels()
    private val isBluetoothEnabled = mutableStateOf(false)
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var exampleBluetooth: ExampleBluetooth? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> isBluetoothEnabled.value = false
                    BluetoothAdapter.STATE_ON -> isBluetoothEnabled.value = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothAdapter = (this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        isBluetoothEnabled.value = bluetoothAdapter.isEnabled
        setContent {
            Ble_exampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isBluetoothEnabled.value) {
                        BluetoothColumn()
                    } else {
                        Error()
                    }
                }
            }
        }
    }

    @Composable
    private fun BluetoothColumn() {
        var mac by remember {
            mutableStateOf(this.getString(R.string.empty))
        }
        var whatToSend by remember {
            mutableStateOf(this.getString(R.string.empty))
        }
        var receivedData by remember {
            mutableStateOf(this.getString(R.string.empty))
        }
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(value = mac, onValueChange = { mac = it })
            Spacer(modifier = Modifier.fillMaxHeight(0.03f))
            Button(onClick = {
                if (mac.isNotEmpty()) {
                    if (exampleBluetooth != null) {
                        exampleBluetooth!!.disconnect()
                    }
                    exampleBluetooth =
                        ExampleBluetooth(this@MainActivity, viewModel, bluetoothAdapter, mac) {
                            receivedData = it
                        }
                    exampleBluetooth!!.connect()
                    Toast.makeText(
                        this@MainActivity,
                        this@MainActivity.getString(R.string.connected),
                        Toast.LENGTH_LONG
                    ).show()
                } else Toast.makeText(
                    this@MainActivity,
                    this@MainActivity.getString(R.string.input_mac),
                    Toast.LENGTH_LONG
                ).show()
            }) {
                Text(stringResource(id = R.string.connect))
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.03f))
            TextField(value = whatToSend, onValueChange = { whatToSend = it })
            Spacer(modifier = Modifier.fillMaxHeight(0.03f))
            Button(onClick = {
                if (whatToSend.isNotEmpty() && exampleBluetooth != null) {
                    exampleBluetooth!!.sendData(whatToSend)
                } else Toast.makeText(
                    this@MainActivity,
                    this@MainActivity.getString(R.string.input_data_to_send),
                    Toast.LENGTH_LONG
                ).show()
            }) {
                Text(stringResource(R.string.send_data))
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.03f))
            Button(onClick = {
                if (exampleBluetooth != null) exampleBluetooth!!.disconnect()
                exampleBluetooth = null
                Toast.makeText(
                    this@MainActivity,
                    this@MainActivity.getString(R.string.disconnected),
                    Toast.LENGTH_LONG
                ).show()
            }) {
                Text(stringResource(R.string.disconnect))
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.03f))
            LazyColumn(Modifier.fillMaxHeight(0.4f)) {
                item {
                    if (receivedData.isNotEmpty()) Text(text = "Данные получены:\n$receivedData")
                }
            }
        }
    }

    @Composable
    private fun Error() {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(id = R.string.turn_on_bluetooth))
        }
    }

    override fun onStart() {
        registerReceiver(broadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(broadcastReceiver)
        super.onStop()
    }
}