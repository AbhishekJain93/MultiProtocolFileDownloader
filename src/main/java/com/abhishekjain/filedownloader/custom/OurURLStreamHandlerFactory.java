package com.abhishekjain.filedownloader.custom;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class OurURLStreamHandlerFactory implements URLStreamHandlerFactory {
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equalsIgnoreCase("crypt"))
            return new Handler();
        else
            return null;
    }
}