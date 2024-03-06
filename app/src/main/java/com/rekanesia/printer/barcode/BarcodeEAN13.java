package com.rekanesia.printer.barcode;

import com.pupukindonesia.rmsandroid.printer.EscPosPrinterCommands;
import com.pupukindonesia.rmsandroid.printer.EscPosPrinterSize;
import com.pupukindonesia.rmsandroid.printer.exceptions.EscPosBarcodeException;

public class BarcodeEAN13 extends BarcodeNumber {

    public BarcodeEAN13(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_EAN13, code, widthMM, heightMM, textPosition);
    }

    @Override
    public int getCodeLength() {
        return 13;
    }
}
