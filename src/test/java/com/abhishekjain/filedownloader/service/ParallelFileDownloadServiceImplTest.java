package com.abhishekjain.filedownloader.service;

import com.abhishekjain.filedownloader.manager.DownloadManager;
import com.abhishekjain.filedownloader.model.DownloadStatus;
import com.abhishekjain.filedownloader.model.FileDownloadResult;
import com.abhishekjain.filedownloader.utils.FileDownloaderUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ParallelFileDownloadServiceImplTest {

    @Spy
    @InjectMocks
    private ParallelFileDownloadServiceImpl downloadService = new ParallelFileDownloadServiceImpl();

    @Mock
    private DownloadManager downloadManager;

    @Mock
    private FileDownloaderUtils fileDownloaderUtils;

    private FileDownloaderUtils downloaderUtils = new FileDownloaderUtils();

    @Before
    public void before() throws IllegalAccessException {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10,
                                                             10, 1000, TimeUnit
                                                                     .MILLISECONDS, new
                                                                     LinkedBlockingQueue<>(100),
                                                             new ThreadPoolExecutor
                                                                     .CallerRunsPolicy());
        FieldUtils.writeField(downloadService, "threadExecutor", executor, true);
        FieldUtils.writeField(downloadService, "poolSize", 5, true);
    }

    @Test
    public void downloadFilesFromSources_valid_test() {

        when(downloadManager.downloadFromSource(anyString(), anyString()))
                .thenReturn(new FileDownloadResult().setDownloadStatusStatus(DownloadStatus.COMPLETED)
                                                    .setMessage
                                                            ("Done"));

        when(fileDownloaderUtils.isValidUrl(anyString())).thenReturn(true);

        downloadService.downloadFilesFromSources(Arrays.asList(
                "http://www.africau.edu/images/default/sample.pdf",
                "http://insight.dev.schoolwires" +
                        ".com/HelpAssets/C2Assets/C2Files/C2ImportCalEventSample.csv"), "download");
        Assert.assertNotNull(downloadService);

    }

    @Test
    public void downloadFilesFromSources_no_url_passed_test() throws MalformedURLException {

        when(downloadManager.downloadFromSource(anyString(), anyString()))
                .thenReturn(new FileDownloadResult().setDownloadStatusStatus(DownloadStatus.COMPLETED)
                                                    .setMessage
                                                            ("Done"));

        when(fileDownloaderUtils.isValidUrl(anyString())).thenReturn(true);

        downloadService.downloadFilesFromSources(Collections.emptyList(), "download");

        Assert.assertNotNull(downloadService);

        String saveLocation = downloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                     ".edu/images/default/sample.pdf")
                , "download");

        Assert.assertFalse(Files.exists(Paths.get(saveLocation)));

    }

    @Test
    public void initMethod_test() {

        downloadService.init();

        Assert.assertNotNull(downloadService);

    }

}
