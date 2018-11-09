package com.abhishekjain.filedownloader;

import com.abhishekjain.filedownloader.configuration.FileDownloaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileDownloaderLauncher implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FileDownloaderLauncher.class);

    @Autowired
    FileDownloaderConfig downloaderConfig;

    public static void main(String[] args) {

        SpringApplication.run(FileDownloaderLauncher.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("default dir : {}", downloaderConfig.getDefaultDirectory());
        log.info("base dir : {}", args.getOptionValues("directory"));
        log.info("urls : {}", args.getOptionValues("urls"));
    }
}
