package com.abhishekjain.filedownloader.manager;

import com.abhishekjain.filedownloader.model.DownloadStatus;
import com.abhishekjain.filedownloader.model.FileDownloadResult;
import com.abhishekjain.filedownloader.utils.FileDownloaderUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * The implementation class that performs core IO for downloading tasks
 * and wraps the retry logic over it.
 */
@Service("RetryableDownloadManager")
public class RetryableDownloadManagerImpl implements DownloadManager {
    private static final Logger log = LoggerFactory.getLogger(RetryableDownloadManagerImpl.class);

    @Autowired
    private
    RetryTemplate retryTemplate;

    @Autowired
    private FileDownloaderUtils fileDownloaderUtils;

    /**
     * The method wraps the downloading IO logic with Spring-Retry template.
     * This allows for retrying over the specified time in case of execeptions like
     * {@code {@link java.net.ConnectException}, {@link} {@link IOException}}
     *
     * @param source          Source url to download the file from
     * @param outputDirectory The final directory to save the downloaded file
     * @return @{@link FileDownloadResult} Final result od downloading the file
     */
    @Override
    public FileDownloadResult downloadFromSource(String source, String outputDirectory) {

        try {
            return retryTemplate.execute(
                    arg -> downloadAndSave(source, outputDirectory),
                    arg -> {

                        fileDownloaderUtils.deleteFileQuietly(source, outputDirectory);
                        log.error(
                                "Retry attempts exhausted to " +
                                        "download from sources: {}." +
                                        "Hence deleting the temp/partially downloaded file and returning as ERROR",
                                source);

                        return new FileDownloadResult()
                                .setDownloadStatusStatus(DownloadStatus
                                                                 .ERROR)
                                .setMessage("RetryAttemptsExhausted");

                    });
        } catch (Exception e) {
            log.error("Non recoverable Exception occurred while downloading from source: {}. Exception: {}", source,
                      e.getMessage());

            fileDownloaderUtils.deleteFileQuietly(source, outputDirectory);
            return new FileDownloadResult().setDownloadStatusStatus(DownloadStatus.ERROR).setMessage
                    (e.getMessage());
        }

    }

    /**
     * The method employs the java NIO package for downloading the files.
     * <p>
     *      The Java NIO package offers the possibility to transfer bytes between 2 Channels without buffering them into
     *      the application memory.
     *
     *      The transferTo() and transferFrom() methods are more efficient than simply reading from a stream using a
     *      buffer. Depending on the underlying operating system, the data can be transferred directly from the filesystem
     *      cache to our file without copying any bytes into the application memory.
     *
     *      On Linux and UNIX systems, these methods use the zero-copy technique that reduces the number of context
     *      switches between the kernel mode and user mode.
     * </p>
     *
     * @param source          Source url to download the file from
     * @param outputDirectory The final directory to save the downloaded file
     * @return @{@link FileDownloadResult} Final result od downloading the file
     * @throws IOException
     */
    private FileDownloadResult downloadAndSave(String source, String outputDirectory) throws IOException {

        URL sourceUrl = new URL(source);
        String downloadFileName = fileDownloaderUtils.uniqueFileSaveLocation(sourceUrl, outputDirectory);

        FileUtils.touch(new File(downloadFileName));
        FileOutputStream fileOutputStream = new FileOutputStream(downloadFileName);

        try (ReadableByteChannel readableByteChannel = Channels.newChannel(fileDownloaderUtils.openStream(sourceUrl))
             ; FileChannel fileChannel = fileOutputStream.getChannel()) {

            fileChannel
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

        } catch (MalformedURLException e) {

            log.error("Unable to download file: {} due to invalid/malformed source. Exception: {}", source, e
                    .getMessage());
            return new FileDownloadResult().setDownloadStatusStatus(DownloadStatus.ERROR).setMessage("MalformedURL");

        }

        log.info("Download for source: {} completed successfully at path: {}", source, downloadFileName);
        return new FileDownloadResult().setDownloadStatusStatus(DownloadStatus.COMPLETED).setMessage("Download " +
                                                                                                             "completed for source: " + source);
    }

}
