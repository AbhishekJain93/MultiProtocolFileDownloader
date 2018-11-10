package com.abhishekjain.filedownloader.manager;

import com.abhishekjain.filedownloader.model.FileDownloadResult;

public interface DownloadManager {
    FileDownloadResult downloadFromSource(String source, String outputDirectory);
}
