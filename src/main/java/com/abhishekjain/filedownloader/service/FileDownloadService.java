package com.abhishekjain.filedownloader.service;

import java.util.List;

/**
 * Base Interface of the Download Service
 * Exposes a single method ${@link #downloadFilesFromSources(List, String)}
 */
public interface FileDownloadService {

    void downloadFilesFromSources(final List<String> sources, final String outputDirectory);
}
