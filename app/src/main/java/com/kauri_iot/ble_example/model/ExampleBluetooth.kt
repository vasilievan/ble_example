package com.kauri_iot.ble_example.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kauri_iot.ble_example.R
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class ExampleBluetooth(
    private val context: Context,
    private val viewModel: AndroidViewModel,
    adapter: BluetoothAdapter,
    mac: String,
    setCallback: (String) -> Unit
) {
    private val device = adapter.getRemoteDevice(mac)
    private val activatorBluetoothGattCallback = ExampleBluetoothGattCallback(setCallback)
    private var gatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    fun connect() {
        gatt = device.connectGatt(context, false, activatorBluetoothGattCallback)
    }

    fun sendData(data: String) {
        viewModel.viewModelScope.launch {
            activatorBluetoothGattCallback.performWriteCharacteristic(
                data.split(context.getString(R.string.one_space)).map { it.toByte() }.toByteArray()
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        viewModel.viewModelScope.launch {
            gatt?.apply {
                close()
                disconnect()
            }
        }
    }
}