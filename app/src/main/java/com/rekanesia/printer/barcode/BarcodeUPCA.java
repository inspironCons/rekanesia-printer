package com.rekanesia.printer.barcode;

import com.pupukindonesia.rmsandroid.printer.EscPosPrinterCommands;
import com.pupukindonesia.rmsandroid.printer.EscPosPrinterSize;
import com.pupukindonesia.rmsandroid.printer.exceptions.EscPosBarcodeException;

public class BarcodeUPCA extends BarcodeNumber {

    public BarcodeUPCA(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_UPCA, code, widthMM, heightMM, textPosition);
    }

    @Override
    public int getCodeLength() {
        return 12;
    }
}
