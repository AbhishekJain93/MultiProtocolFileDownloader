package com.abhishekjain.filedownloader.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.apache.commons.validator.routines.UrlValidator.ALLOW_2_SLASHES;
import static org.apache.commons.validator.routines.UrlValidator.ALLOW_ALL_SCHEMES;
import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;

@Service
public class FileDownloaderUtils {

    private static final UrlValidator defaultValidator = new UrlValidator(ALLOW_ALL_SCHEMES | ALLOW_2_SLASHES |
                                                                                  ALLOW_LOCAL_URLS);

    @Value("${download.connect.timeout}")
    private int connectTimeout;

    @Value("${download.read.timeout}")
    private int readTimeout;

    public boolean isValidUrl(String url) {

        return defaultValidator.isValid(url);

    }

    public String uniqueFileSaveLocation(URL sourceUrl, String saveDirectory) {

        return saveDirectory.concat(File.separator).concat(uniqueFilenameForSource(sourceUrl));
    }

    private String uniqueFilenameForSource(URL url) {

        StringBuilder filenameBuilder = new StringBuilder();
        filenameBuilder.append(url.getProtocol())
                .append('-')
                .append(url.getPort() == -1 ? url.getDefaultPort() : url.getPort())
                .append('-')
                .append(url.getHost())
                .append('-')
                .append(url.getPath());
        if (StringUtils.isEmpty(url.getPath())) {
            filenameBuilder.deleteCharAt(filenameBuilder.length() - 1);
        }

        return DigestUtils.sha1Hex(filenameBuilder.toString())
                .concat("_").concat(FilenameUtils.getName(url.getPath()));

    }

    public InputStream openStream(URL url) throws IOException {

        URLConnection conn = url.openConnection();

        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        return conn.getInputStream();
    }

    public void deleteFileQuietly(String source, String outputDirectory) {

        URL sourceUrl;
        try {
            sourceUrl = new URL(source);
        } catch (MalformedURLException ignored) {
            return;
        }
        String downloadFileName = uniqueFileSaveLocation(
                sourceUrl,
                outputDirectory);

        FileUtils.deleteQuietly(new File(downloadFileName));

    }
}
