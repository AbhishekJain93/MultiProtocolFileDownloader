package com.abhishekjain.filedownloader.custom;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class encapsulating the encoding logic for custom rot13 protocol.
 */
public class rot13InputStream extends FilterInputStream {

    public rot13InputStream(InputStream i) {
        super(i);
    }

    public int read() throws IOException {
        return rot13(in.read());
    }

    /**
     * Rotates the input int to encode the stream of charaters
     *
     * @param c int to be transformed
     * @return the rotated int by 13 modulo 26
     */
    private int rot13(int c) {
        if ((c >= 'A') && (c <= 'Z')) c = (((c - 'A') + 13) % 26) + 'A';
        if ((c >= 'a') && (c <= 'z'))
            c = (((c - 'a') + 13) % 26) + 'a';
        return c;
    }
}
