package com.pupukindonesia.rmsandroid.printer.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.pupukindonesia.rmsandroid.printer.connection.DeviceConnection
import com.pupukindonesia.rmsandroid.printer.exceptions.EscPosConnectionException
import java.io.IOException
import java.util.UUID

class BluetoothConnection
    /**
     * Create un instance of BluetoothConnection.
     *
     * @param device an instance of BluetoothDevice
     */(
    /**
     * Get the instance BluetoothDevice connected.
     *
     * @return an instance of BluetoothDevice
     */
    val device: BluetoothDevice?,
    private val context: Context
) : DeviceConnection() {

    private var socket: BluetoothSocket? = null

    /**
     * Check if OutputStream is open.
     *
     * @return true if is connected
     */
    override fun isConnected(): Boolean {
        return socket != null && socket!!.isConnected && super.isConnected()
    }

    /**
     * Start socket connection with the bluetooth device.
     */
    @SuppressLint("MissingPermission")
    @Throws(EscPosConnectionException::class)
    override fun connect(): BluetoothConnection {
        if (this.isConnected) {
            return this
        }
        Firebase.crashlytics.log("Bluetooth device is not connected. device : $device")
        if (device == null) {
            throw EscPosConnectionException("Bluetooth device is not connected.")
        }
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val uuids = device.uuids
        val uuid = if (uuids != null && uuids.isNotEmpty()) uuids[0].uuid else UUID.randomUUID()
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter.cancelDiscovery()
            socket!!.connect()
            outputStream = socket!!.outputStream
            data = ByteArray(0)
        } catch (e: IOException) {
            try {
                socket = device.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType
                    )
                ).invoke(device, 1) as BluetoothSocket
                bluetoothAdapter.cancelDiscovery()
                socket!!.connect()
                outputStream = socket!!.outputStream
                data = ByteArray(0)
            }catch (e:IOException){
                disconnect()
                throw EscPosConnectionException("Unable to connect to bluetooth device.")
            }
        }
        return this
    }

    /**
     * Close the socket connection with the bluetooth device.
     */
    override fun disconnect(): BluetoothConnection {
        data = ByteArray(0)
        if (outputStream != null) {
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            outputStream = null
        }
        if (socket != null) {
            try {
                socket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            socket = null
        }
        return this
    }
}