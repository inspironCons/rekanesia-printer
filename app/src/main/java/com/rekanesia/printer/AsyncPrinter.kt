package com.rekanesia.printer

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.rekanesia.printer.EscPosCharsetEncoding
import com.rekanesia.printer.EscPosPrinter
import com.rekanesia.printer.EscPosPrinterSize
import com.rekanesia.printer.connection.DeviceConnection
import com.rekanesia.printer.exceptions.EscPosBarcodeException
import com.rekanesia.printer.exceptions.EscPosConnectionException
import com.rekanesia.printer.exceptions.EscPosEncodingException
import com.rekanesia.printer.exceptions.EscPosParserException
import java.lang.ref.WeakReference

class AsyncEscPosPrinter(
    val printerConnection: DeviceConnection?,
    printerDpi: Int,
    printerWidthMM: Float,
    printerNbrCharactersPerLine: Int
) :
    EscPosPrinterSize(printerDpi, printerWidthMM, printerNbrCharactersPerLine) {
    var textToPrint = ""
        private set

    fun setTextToPrint(textToPrint: String): AsyncEscPosPrinter {
        this.textToPrint = textToPrint
        return this
    }

}

open class AsyncEscPosPrint(context: Context?) : AsyncTask<AsyncEscPosPrinter?, Int?, Int>() {
    protected var dialog: ProgressDialog? = null
    private var weakContext: WeakReference<Context> = WeakReference(context)
    private lateinit var printer: EscPosPrinter

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: AsyncEscPosPrinter?): Int? {
        if (params.isEmpty()) {
            return FINISH_NO_PRINTER
        }
        publishProgress(PROGRESS_CONNECTING)
        val printerData = params[0]
        try {
            val deviceConnection: DeviceConnection = printerData?.printerConnection
                ?: return FINISH_NO_PRINTER
            printer = EscPosPrinter(
                deviceConnection,
                printerData.printerDpi,
                printerData.printerWidthMM,
                printerData.printerNbrCharactersPerLine,
                EscPosCharsetEncoding(
                    "utf-8",
                    0xFF
                )
            )
            publishProgress(PROGRESS_PRINTING)
            printer.printFormattedTextAndCut(printerData.textToPrint)
            publishProgress(PROGRESS_PRINTED)
        } catch (e: EscPosConnectionException) {
//            Firebase.crashlytics.recordException(RuntimeException(
//                "PRINTER EscPosConnectionException exception",e
//            ))
            return FINISH_PRINTER_DISCONNECTED
        } catch (e: EscPosParserException) {
//            Firebase.crashlytics.recordException(RuntimeException(
//                "PRINTER EscPosParserException exception",e
//            ))
            return FINISH_PARSER_ERROR
        } catch (e: EscPosEncodingException) {
//            Firebase.crashlytics.recordException(RuntimeException(
//                "PRINTER EscPosEncodingException exception",e
//            ))
            return FINISH_ENCODING_ERROR
        } catch (e: EscPosBarcodeException) {
//            Firebase.crashlytics.recordException(RuntimeException(
//                "PRINTER EscPosBarcodeException exception",e
//            ))
            return FINISH_BARCODE_ERROR
        }
        return FINISH_SUCCESS
    }

    @Deprecated("Deprecated in Java")
    override fun onPreExecute() {
        if (dialog == null) {
            try {
                val context = weakContext.get() as AppCompatActivity
                if (isCancelled) {
                    return
                } else {
                    if(!context.isFinishing){
                        dialog = ProgressDialog(context)
                        dialog?.setTitle("Proses...")
                        dialog?.setMessage("...")
                        dialog?.setProgressNumberFormat("%1d / %2d")
                        dialog?.setCancelable(false)
                        dialog?.isIndeterminate = false
                        dialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                        dialog?.show()
                    }
                }
            }catch (e:Exception){
//                Firebase.crashlytics.recordException(RuntimeException(
//                    "PRINTER onPreExecute exception",e
//                ))
            }

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onProgressUpdate(vararg values: Int?) {
        when (values[0]) {
            PROGRESS_CONNECTING -> dialog?.setMessage("Menyandikan printer...")
            PROGRESS_CONNECTED -> dialog?.setMessage("Printer telah terhubung...")
            PROGRESS_PRINTING -> dialog?.setMessage("Printer sedang Mencetak...")
            PROGRESS_PRINTED -> dialog?.setMessage("Printer selesai mencetak...")
        }
        dialog?.progress = values[0]!!
        dialog?.max = 4
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: Int) {
        dialog?.dismiss()
        dialog = null
        val context = weakContext.get() ?: return
        try {
            if (!isCancelled) {
                when (result) {
                    FINISH_SUCCESS -> AlertDialog.Builder(context)
                        .setTitle("Berhasil")
                        .setMessage("Printer selesai mencetak")
                        .setPositiveButton("Ok"){_,_->
                            printer.disconnectPrinter()
                        }
                        .show()
                    FINISH_NO_PRINTER -> {
                        AlertDialog.Builder(context)
                            .setTitle("Printer tidak ditemukan")
                            .setMessage("Aplikasi tidak bisa menemukan printer, harap sandingkan printer")
                            .setPositiveButton("Ok") { _, _ ->
                                context.startActivity(
                                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                )
                            }
                            .show()

                    }
                    FINISH_PRINTER_DISCONNECTED -> AlertDialog.Builder(context)
                        .setTitle("Koneksi Rusak")
                        .setMessage("Tidak bisa terhubung ke printer")
                        .show()
                    FINISH_PARSER_ERROR -> AlertDialog.Builder(context)
                        .setTitle("Invalid formatted text")
                        .setMessage("It seems to be an invalid syntax problem.")
                        .show()
                    FINISH_ENCODING_ERROR -> AlertDialog.Builder(context)
                        .setTitle("Bad selected encoding")
                        .setMessage("The selected encoding character returning an error.")
                        .show()
                    FINISH_BARCODE_ERROR -> AlertDialog.Builder(context)
                        .setTitle("Invalid barcode")
                        .setMessage("data send to be converted to barcode or QR code seems to be invalid.")
                        .show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        const val FINISH_SUCCESS = 1
        const val FINISH_NO_PRINTER = 2
        const val FINISH_PRINTER_DISCONNECTED = 3
        const val FINISH_PARSER_ERROR = 4
        const val FINISH_ENCODING_ERROR = 5
        const val FINISH_BARCODE_ERROR = 6
        const val PROGRESS_CONNECTING = 1
        const val PROGRESS_CONNECTED = 2
        const val PROGRESS_PRINTING = 3
        const val PROGRESS_PRINTED = 4
    }
}

class AsyncBluetoothEscPosPrint(context: Context?) : AsyncEscPosPrint(context) {
    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: AsyncEscPosPrinter?): Int? {
        if (params.isEmpty()) {
            return FINISH_NO_PRINTER
        }
        val printerData = params[0]

        val deviceConnection: DeviceConnection? = printerData?.printerConnection
        publishProgress(PROGRESS_CONNECTING)
        try {
            deviceConnection?.connect()
        } catch (e: EscPosConnectionException) {
//            Firebase.crashlytics.recordException(RuntimeException(
//                "PRINTER AsyncBluetoothEscPosPrint doInBackground exception while connect",e
//            ))
        }
        return super.doInBackground(*params)
    }
}
