package com.rekanesia.printer.textparser;

import com.rekanesia.printer.EscPosPrinterCommands;
import com.rekanesia.printer.exceptions.EscPosConnectionException;
import com.rekanesia.printer.exceptions.EscPosEncodingException;

public interface IPrinterTextParserElement {
    int length() throws EscPosEncodingException;
    IPrinterTextParserElement print(EscPosPrinterCommands printerSocket) throws EscPosEncodingException, EscPosConnectionException;
}
