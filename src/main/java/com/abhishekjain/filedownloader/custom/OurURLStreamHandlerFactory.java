package com.abhishekjain.filedownloader.custom;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Register {@link OurURLStreamHandlerFactory} during application's startup via URL#setURLStreamHandlerFactory()
 * <p>
 *      Note that the Javadoc explicitly says that you can set it at most once. So if you intend to support multiple
 *      custom protocols in the same application, you'd need to generify the custom URLStreamHandlerFactory implementation
 *      to cover them all inside the createURLStreamHandler() method.
 * </p>
 */
public class OurURLStreamHandlerFactory implements URLStreamHandlerFactory {
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equalsIgnoreCase("rot13"))
            return new Handler();
        else
            return null;
    }
}