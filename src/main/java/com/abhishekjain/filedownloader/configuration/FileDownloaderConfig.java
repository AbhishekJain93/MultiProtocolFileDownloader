package com.abhishekjain.filedownloader.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration class to save the default save directory if not provided while running the application.
 */
@Component
public class FileDownloaderConfig {

    private String defaultDirectory;

    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    @Value("${download.base.dir}")
    public void setDefaultDirectory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }
}
