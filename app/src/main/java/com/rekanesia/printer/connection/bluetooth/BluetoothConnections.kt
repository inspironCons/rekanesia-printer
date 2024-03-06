package com.pupukindonesia.rmsandroid.printer.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context

open class BluetoothConnections(
    private val context:Context
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    /**
     * Get a list of Bluetooth devices available.
     * @return Return an array of BluetoothConnection instance
     */
    @SuppressLint("MissingPermission")
    fun list(): Array<BluetoothConnection>? {
        if (bluetoothManager.adapter == null) {
            return null
        }

        if (!bluetoothManager.adapter.isEnabled) {
            return null
        }

        val bluetoothDevicesList: Set<BluetoothDevice> = bluetoothManager.adapter.bondedDevices
        val bluetoothDevices = Array(bluetoothDevicesList.size) { BluetoothConnection(null,context) }

        if (bluetoothDevicesList.isNotEmpty()) {
            var i = 0
            for (device in bluetoothDevicesList) {
                bluetoothDevices[i++] = BluetoothConnection(device,context)
            }
        }

        return bluetoothDevices
    }
}