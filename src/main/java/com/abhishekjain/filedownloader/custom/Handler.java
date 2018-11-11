package com.abhishekjain.filedownloader.custom;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Custom Handler called to parseUrl
 * and open the connection for streams to work
 */
public class Handler extends URLStreamHandler {

    protected void parseURL(URL url, String spec,
                            int start, int end) {
        int slash = spec.indexOf('/');
        String cryptType = spec.substring(0, slash - 1);
        super.parseURL(url, spec, slash, end);
        setURL(url, cryptType, url.getHost(),
               url.getPort(), url.getAuthority(), url.getUserInfo(), url.getPath(), url.getQuery(), url.getRef());
    }

    protected URLConnection openConnection(URL url)
            throws IOException {
        String crypType = url.getProtocol();
        return new CryptURLConnection(url, crypType);
    }
}