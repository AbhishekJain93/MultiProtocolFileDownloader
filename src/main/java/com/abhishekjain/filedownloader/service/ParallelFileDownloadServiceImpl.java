package com.abhishekjain.filedownloader.service;

import com.abhishekjain.filedownloader.configuration.FileDownloaderConfig;
import com.abhishekjain.filedownloader.manager.DownloadManager;
import com.abhishekjain.filedownloader.model.FileDownloadResult;
import com.abhishekjain.filedownloader.utils.FileDownloaderUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of @{@link FileDownloadService}
 * This implementation creates @{@link CompletableFuture} of the download tasks
 * and submits them to be <i>processed in parallel</i>.
 */
@Service
public class ParallelFileDownloadServiceImpl implements FileDownloadService {
    private static final Logger log = LoggerFactory.getLogger(ParallelFileDownloadServiceImpl.class);

    @Autowired
    FileDownloaderConfig downloaderConfig;

    @Autowired
    private FileDownloaderUtils fileDownloaderUtils;

    @Autowired
    @Qualifier("RetryableDownloadManager")
    private DownloadManager downloadManager;

    @Value("${download.pool.threads}")
    private int poolSize;

    private ThreadPoolExecutor threadExecutor;

    /**
     * Initialises the thread pool that controls the number of workers available
     * for the download tasks. Property {@link #poolSize} is used to configure
     * the thread pool size.
     */
    @PostConstruct
    private void init() {

        threadExecutor = new ThreadPoolExecutor(poolSize,
                                                poolSize, 1000, TimeUnit
                                                        .MILLISECONDS, new
                                                        LinkedBlockingQueue<>(100),
                                                new ThreadPoolExecutor
                                                        .CallerRunsPolicy());

        log.info("Initialized task pool [for parallel download] of size :{}", poolSize);
    }

    /**
     * The method takes the input url sources and prepares them to be downloaded in parallel.
     *
     * @param sources         The Url sources from where to download from.
     * @param outputDirectory The final directory where the downloaded files would be saved
     */
    @Override
    public void downloadFilesFromSources(List<String> sources, String outputDirectory) {

        List<String> validSources = sources.stream().filter(url -> fileDownloaderUtils.isValidUrl(url)).collect
                (Collectors
                         .toList());

        log.warn("Invalid/Malformed sources that won't be attempted for download : {}", CollectionUtils.subtract
                (sources, validSources));

        CompletableFuture.allOf(
                validSources
                        .stream().map(source -> downloadAndSaveTask(source, outputDirectory)).toArray
                        (CompletableFuture[]::new)
        )
                .thenAccept(result -> {
                    log.info("Download of all the sources have been completed/terminated. Program would exit now");
                    threadExecutor.shutdownNow();
                });

    }

    private CompletableFuture<FileDownloadResult> downloadAndSaveTask(String source, String saveDirectory) {

        return CompletableFuture.supplyAsync(
                () -> downloadManager.downloadFromSource(source, saveDirectory),
                threadExecutor);

    }

}
