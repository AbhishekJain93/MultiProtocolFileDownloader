package com.abhishekjain.filedownloader.custom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The {@link InputStream} sub class to encapsulate the logic
 * to encode using rot13[rotate-13]
 * and would be used by custom {@link CryptURLConnection}
 */
abstract class CryptInputStream extends InputStream {
    InputStream in;
    OutputStream out;

    abstract public void set(InputStream in, OutputStream out);
}

class rot13CryptInputStream extends CryptInputStream {

    public void set(InputStream in, OutputStream out) {
        this.in = new rot13InputStream(in);
    }

    public int read() throws IOException {
        return in.read();
    }
}