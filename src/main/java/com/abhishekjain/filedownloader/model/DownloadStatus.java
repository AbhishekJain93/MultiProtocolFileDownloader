package com.abhishekjain.filedownloader.model;

/**
 * Enum enumerating the Status of the download task
 */
public enum DownloadStatus {

    COMPLETED("COMPLETED"), ERROR("ERROR"), IN_PROGRESS("IN_PROGRESS");

    private String status;

    DownloadStatus(String status) {

        this.status = status;
    }
}
