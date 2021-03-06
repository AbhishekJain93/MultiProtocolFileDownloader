package com.abhishekjain.filedownloader.custom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class extending {@link URLConnection} for the custom rot13 protocol.
 * It opens the input stream @{@link CryptInputStream}
 * from the URL and process the streams
 */
class CryptURLConnection extends URLConnection {
    static int defaultPort = 80;
    CryptInputStream cis;

    CryptURLConnection(URL url, String cryptype) {
        super(url);
        try {
            String name = "com.abhishekjain.filedownloader.custom." + cryptype
                    + "CryptInputStream";
            cis = (CryptInputStream) Class.forName(name).newInstance();
        } catch (Exception e) {
        }
    }

    synchronized public void connect() throws IOException {
        int port;
        if (cis == null)
            throw new IOException("Crypt Class Not Found");
        if ((port = url.getPort()) == -1)
            port = defaultPort;
        Socket s = new Socket(url.getHost(), port);

        // Send the filename in plaintext
        OutputStream server = s.getOutputStream();
        PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(server, "UTF-8"),
                true);

        // Initialize the CryptInputStream
        cis.set(s.getInputStream(), server);
        connected = true;
    }

    synchronized public InputStream getInputStream()
            throws IOException {
        if (!connected)
            connect();
        return (cis);
    }

    public String getContentType() {
        return guessContentTypeFromName(url.getFile());
    }
}

