package com.abhishekjain.filedownloader.manager;

import com.abhishekjain.filedownloader.model.FileDownloadResult;

/**
 * The interface defining single method : {@link #downloadFromSource(String, String)}
 * The implementations would do the core heavyweight task of opening the stream
 * and creating Channels and perform the IO.
 */
public interface DownloadManager {
    FileDownloadResult downloadFromSource(String source, String outputDirectory);
}
