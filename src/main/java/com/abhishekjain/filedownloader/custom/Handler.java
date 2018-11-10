package com.abhishekjain.filedownloader.custom;

import java.io.*;
import java.net.*;

public class Handler extends URLStreamHandler {

    protected void parseURL(URL url, String spec,
                            int start, int end) {
        int slash = spec.indexOf('/');
        String crypType = spec.substring(start, slash-1);
        super.parseURL(url, spec, slash, end);
        setURL( url, "crypt:"+crypType, url.getHost( ),
                url.getPort(), url.getFile(), url.getRef( ) );
    }

    protected URLConnection openConnection(URL url)
            throws IOException {
        String crypType = url.getProtocol( ).substring(6);
        return new CryptURLConnection( url, crypType );
    }
}