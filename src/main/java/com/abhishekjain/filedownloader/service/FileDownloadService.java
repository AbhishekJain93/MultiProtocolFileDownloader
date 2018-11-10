package com.abhishekjain.filedownloader.service;

import java.util.List;

public interface FileDownloadService {

    void downloadFilesFromSources(List<String> sources, String outputDirectory);
}
