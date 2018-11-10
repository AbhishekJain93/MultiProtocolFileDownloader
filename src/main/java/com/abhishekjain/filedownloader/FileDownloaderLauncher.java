package com.abhishekjain.filedownloader;

import com.abhishekjain.filedownloader.configuration.FileDownloaderConfig;
import com.abhishekjain.filedownloader.custom.OurURLStreamHandlerFactory;
import com.abhishekjain.filedownloader.service.FileDownloadService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@EnableRetry
@EnableAspectJAutoProxy
@SpringBootApplication
public class FileDownloaderLauncher implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FileDownloaderLauncher.class);

    @Autowired
    private
    FileDownloaderConfig downloaderConfig;

    @Autowired
    private
    FileDownloadService fileDownloadService;

    public static void main(String[] args) {

        URL.setURLStreamHandlerFactory(new OurURLStreamHandlerFactory());
        
        new SpringApplicationBuilder(FileDownloaderLauncher.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) {

        List<String> urls = urlSources(args.getOptionValues("urls"));
        String outputDirectory = CollectionUtils.isEmpty(args.getOptionValues("directory")) ? "" : args.getOptionValues
                ("directory").get(0);

        log.info("Default directory to download files is set : {}", downloaderConfig.getDefaultDirectory());
        log.info("Overriding directory passed : {}", outputDirectory);
        log.info("Sources(Urls) passed : {}", urls);

        if (!CollectionUtils.isEmpty(urls)) {
            fileDownloadService.downloadFilesFromSources(urls, StringUtils.isBlank(outputDirectory) ? downloaderConfig
                    .getDefaultDirectory() : outputDirectory);
        }
    }

    private List<String> urlSources(List<String> arg) {

        if (CollectionUtils.isEmpty(arg)) {
            return Collections.emptyList();
        }

        return Arrays.asList(arg.get(0).split("\\s+"));

    }

}

