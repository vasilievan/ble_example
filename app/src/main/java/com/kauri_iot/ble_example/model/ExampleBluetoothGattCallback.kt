package com.kauri_iot.ble_example.model

import android.annotation.SuppressLint
import android.bluetooth.*
import com.kauri_iot.ble_example.BuildConfig
import java.util.*

class ExampleBluetoothGattCallback(
    private val changeValue: (String) -> Unit
) :
    BluetoothGattCallback() {
    private val serviceUUID = UUID.fromString(BuildConfig.SERVICE_UUID)
    private val characteristicUUIDWrite = UUID.fromString(BuildConfig.CHARACTERISTIC_WRITE_UUID)
    private val characteristicUUIDRead = UUID.fromString(BuildConfig.CHARACTERISTIC_READ_UUID)
    private var gatt: BluetoothGatt? = null
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private val readData = StringBuilder()

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(
        gatt: BluetoothGatt?,
        status: Int,
        newState: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt?.close()
            }
        } else gatt?.close()
        super.onConnectionStateChange(gatt, status, newState)
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        this.gatt = gatt
        val service = gatt?.services?.firstOrNull { it.uuid == serviceUUID }
        readCharacteristic = service?.getCharacteristic(characteristicUUIDRead)
        writeCharacteristic = service?.getCharacteristic(characteristicUUIDWrite)
        gatt?.apply {
            setCharacteristicNotification(readCharacteristic, true)
            setCharacteristicNotification(writeCharacteristic, true)
        }
        val descriptor = readCharacteristic?.descriptors?.first()
        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt?.writeDescriptor(descriptor)
        super.onServicesDiscovered(gatt, status)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        writeCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        readCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        super.onDescriptorWrite(gatt, descriptor, status)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        readData.append(readCharacteristic?.value?.joinToString(" "))
        readData.append("\n")
        changeValue(readData.toString())
        super.onCharacteristicChanged(gatt, characteristic)
    }

    @SuppressLint("MissingPermission")
    fun performWriteCharacteristic(data: ByteArray) {
        writeCharacteristic?.apply {
            writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            value = data
        }
        this.gatt?.writeCharacteristic(writeCharacteristic)
    }
}