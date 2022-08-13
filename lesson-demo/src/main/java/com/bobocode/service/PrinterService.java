package com.bobocode.service;

import com.bobocode.annotation.MagicMarker;

@MagicMarker(value = "myPrinterService", count = 20)
public class PrinterService {
    
    public void hello() {
        System.out.println("Hello");
    }
}
