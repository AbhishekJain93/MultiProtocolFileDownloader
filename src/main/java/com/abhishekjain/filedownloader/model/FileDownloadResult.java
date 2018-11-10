package com.abhishekjain.filedownloader.model;

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
