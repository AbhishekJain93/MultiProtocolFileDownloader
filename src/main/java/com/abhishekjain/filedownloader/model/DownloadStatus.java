package com.abhishekjain.filedownloader.model;

public enum DownloadStatus {

    COMPLETED("COMPLETED"), ERROR("ERROR"), IN_PROGRESS("IN_PROGRESS");

    private String status;

    DownloadStatus(String status) {

        this.status = status;
    }
}
