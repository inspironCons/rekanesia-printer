package com.pupukindonesia.rmsandroid.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pupukindonesia.rmsandroid.printer.connection.bluetooth.BluetoothConnection
import com.pupukindonesia.rmsandroid.printer.connection.bluetooth.BluetoothConnections
import com.pupukindonesia.rmsandroid.printer.exceptions.EscPosConnectionException

class BluetoothPrinters(
    val context: Context
) : BluetoothConnections(context) {
    /**
     * Get a list of bluetooth printers.
     *
     * @return an array of EscPosPrinterCommands
     */
    @SuppressLint("MissingPermission")
    fun getList(): Array<BluetoothConnection?>? {
        val bluetoothDevicesList = super.list() ?: return null
        var i = 0
        val printersTmp = arrayOfNulls<BluetoothConnection>(bluetoothDevicesList.size)
        for (bluetoothConnection in bluetoothDevicesList) {
            val device = bluetoothConnection.device
            if(device != null){
                val majDeviceCl = device.bluetoothClass.majorDeviceClass
                val deviceCl = device.bluetoothClass.deviceClass
                // printer bluetooth pembelian dari jakarta yana bacth pertama eppos dan panda
                val deviceImaging = BluetoothClass.Device.Major.IMAGING

                //printer bluetooth EPPOS yang dari vendor mas mirza itu uncategorized
                val deviceUncategorized = BluetoothClass.Device.Major.UNCATEGORIZED
                Firebase.crashlytics.log("""
                    device scanned, device: ${device.name}
                    device scanned, majDeviceCl: $majDeviceCl
                    device scanned, deviceCl: $deviceCl
                """.trimIndent())
                if ((majDeviceCl == deviceImaging || majDeviceCl == deviceUncategorized) && (deviceCl == 1664 || deviceCl == deviceImaging || deviceCl == deviceUncategorized)) {
                    printersTmp[i++] = BluetoothConnection(device, context = context)
                }

            }else{
                Firebase.crashlytics.log("Device null")
            }
        }
        val bluetoothPrinters = arrayOfNulls<BluetoothConnection>(i)
        System.arraycopy(printersTmp, 0, bluetoothPrinters, 0, i)
        return bluetoothPrinters
    }

    companion object {
        /**
         * Easy way to get the first bluetooth printer paired / connected.
         *
         * @return a EscPosPrinterCommands instance
         */
        fun selectFirstPaired(context: Context): BluetoothConnection? {
            val printers = BluetoothPrinters(context)
            val bluetoothPrinters = printers.getList()
            if (!bluetoothPrinters.isNullOrEmpty()) {
                for (printer in bluetoothPrinters) {
                    try {
                        return printer?.connect()
                    } catch (e: EscPosConnectionException) {
                        Firebase.crashlytics.recordException(
                            RuntimeException("Failed to Connect printer",e)
                        )
                        e.printStackTrace()
                    }
                }
            } else {
                Firebase.crashlytics.recordException(RuntimeException("bluetooh printer non found"))
            }
            return null
        }

        fun selectFirstPaired(context: Context,onResult:((list:BluetoothConnection?)->Unit)) {
            val printers = BluetoothPrinters(context)
            val bluetoothPrinters = printers.getList()
            if (!bluetoothPrinters.isNullOrEmpty()) {
                for (printer in bluetoothPrinters) {
                    try {
                        onResult.invoke(printer?.connect())
                    } catch (e: EscPosConnectionException) {
                        Firebase.crashlytics.recordException(
                            RuntimeException("Failed to Connect printer",e)
                        )
                        e.printStackTrace()
                    }
                }
            } else {
                Firebase.crashlytics.recordException(RuntimeException("bluetooh printer non found"))
            }
            onResult.invoke(null)
        }

        fun checkPermissionAreGranded(context:Context):Boolean{
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                ActivityCompat.checkSelfPermission(context,android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context,android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context,android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
            }else{
                true
            }
        }

        fun permissionBluetooth(context: Context,result:((type:Int)->Unit)){
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) return
            Dexter.withContext(context)
                .withPermissions(
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_ADMIN
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if(p0?.areAllPermissionsGranted() == true){
                            result.invoke(ALL_GRANTED)
                        }else{
                            result.invoke(IS_DENIED)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }
                })
                .onSameThread()
                .check()
        }


        const val ALL_GRANTED = 1
        const val IS_DENIED = 0
    }
}