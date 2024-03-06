package com.rekanesia.printer.textparser;

import com.pupukindonesia.rmsandroid.printer.EscPosPrinterCommands;
import com.pupukindonesia.rmsandroid.printer.exceptions.EscPosConnectionException;
import com.pupukindonesia.rmsandroid.printer.exceptions.EscPosEncodingException;

public interface IPrinterTextParserElement {
    int length() throws EscPosEncodingException;
    IPrinterTextParserElement print(EscPosPrinterCommands printerSocket) throws EscPosEncodingException, EscPosConnectionException;
}
