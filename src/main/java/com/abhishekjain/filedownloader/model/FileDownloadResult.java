package com.abhishekjain.filedownloader.model;

/**
 * The result/response class that contains:
 * 1) @{@link DownloadStatus} Status of the download task
 * 2) {@link #message} Message/Exception code
 */
public class FileDownloadResult {

    private DownloadStatus downloadStatusStatus;
    private String message;

    public DownloadStatus getDownloadStatusStatus() {
        return downloadStatusStatus;
    }

    public FileDownloadResult setDownloadStatusStatus(DownloadStatus downloadStatusStatus) {
        this.downloadStatusStatus = downloadStatusStatus;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public FileDownloadResult setMessage(String message) {
        this.message = message;
        return this;
    }

}
