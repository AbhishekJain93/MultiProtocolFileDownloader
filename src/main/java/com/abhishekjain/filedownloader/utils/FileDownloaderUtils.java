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

    /**
     * Gets the unique path to save file at.
     *
     * <p>
     * Example URL: https://raw.githubusercontent.com/neovim/neovim/master/runtime/doc/filetype.txt
     * <p>
     * Downloaded file will be saved at
     * <b><saveDirectory>/<sha1hex>_<filename></b>
     * where sha1hex is the unique SHA-1 hash constituted from the URL. From example: https
     * host is the host of the protocol.
     * From example: filetype.txt file is the filename in the URL.
     *
     * <i>Note that: 80 is the default port in http. </i>
     * </p>
     *
     * @param sourceUrl,     the URL file belongs to.
     * @param saveDirectory, the Base directory to create file in.
     * @return path to store file at in the localhost.
     */
    public String uniqueFileSaveLocation(URL sourceUrl, String saveDirectory) {

        return saveDirectory.concat(File.separator)
                            .concat(uniqueFilenameForSource(sourceUrl));
    }

    private String uniqueFilenameForSource(URL url) {

        //TODO: ABJ 12112018 Use URL normalization and then sha1hex
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
                          .concat("_")
                          .concat(FilenameUtils.getName(url.getPath()));

    }

    /**
     * The method opens the stream from the URL setting the connection and
     * read timeout from properties file.
     *
     * @param url Url from where stream is to be obtained
     * @return @{@link InputStream} from the url provided
     * @throws IOException If stream can not be obtained
     */
    public InputStream openStream(URL url) throws IOException {

        URLConnection conn = url.openConnection();

        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        return conn.getInputStream();
    }

    /**
     * Deletes the file provided at #outputDirectory silently.
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     *
     * @param source
     * @param outputDirectory
     */
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
