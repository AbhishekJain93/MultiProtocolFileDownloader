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

/**
 * Main Launcher class that contains the main method
 * and parses the arguments passed to the application.
 */

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

    /**
     * Main method that gets invoked when application is run.
     * <pre>
     *   URL.setURLStreamHandlerFactory(new OurURLStreamHandlerFactory());
     * </pre>
     * This line is called to register the custom Protocol Handler to the application
     *
     * @param args Application arguments passed [--urls , --directory]
     */
    public static void main(String[] args) {

        URL.setURLStreamHandlerFactory(new OurURLStreamHandlerFactory());

        new SpringApplicationBuilder(FileDownloaderLauncher.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    /**
     * The @{@link ApplicationRunner} interface method.
     * The urls of the sources are parsed from --urls argument
     * The save directory is parsed from --directory argument. If not passed then
     * #getDefaultDirectory() is used for saving the downloads.
     *
     * @param args
     */
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

        } else {
            log.info("No source has been provided to download. Program would exit now");
        }
    }

    private List<String> urlSources(List<String> arg) {

        if (CollectionUtils.isEmpty(arg)) {
            return Collections.emptyList();
        }

        return Arrays.asList(arg.get(0).split("\\s+"));

    }

}

