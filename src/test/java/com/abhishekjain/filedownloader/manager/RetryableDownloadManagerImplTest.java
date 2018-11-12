package com.abhishekjain.filedownloader.manager;

import com.abhishekjain.filedownloader.configuration.retry.RetryDownloadListener;
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
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RetryableDownloadManagerImplTest {

    @Spy
    @InjectMocks
    private RetryableDownloadManagerImpl downloadManager = new RetryableDownloadManagerImpl();

    @Spy
    private FileDownloaderUtils fileDownloaderUtils;

    @Mock
    private RetryTemplate retryTemplate = new RetryTemplate();

    @Before
    public void before() throws IllegalAccessException {

        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.registerListener(new RetryDownloadListener());

        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setMultiplier(2.0D);
        exponentialBackOffPolicy.setInitialInterval(100L);
        exponentialBackOffPolicy.setMaxInterval(30000L);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        Map<Class<? extends Throwable>, Boolean> exceptionClassifier = new HashMap<>();
        exceptionClassifier.put(IOException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, exceptionClassifier);

        retryTemplate.setRetryPolicy(retryPolicy);

        FieldUtils.writeField(downloadManager, "retryTemplate", retryTemplate, true);
    }

    @Test
    public void downloadFromSource_valid_retry_test() throws MalformedURLException {

        FileDownloadResult downloadResult = downloadManager.downloadFromSource(
                "http://www.africau" +
                        ".edu/images/default/sample.pdf",
                "download");
        String saveLocation = fileDownloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                         ".edu/images/default/sample" +
                                                                                         ".pdf")
                , "download");

        Assert.assertTrue(Files.exists(Paths.get(saveLocation)));
        Assert.assertTrue(DownloadStatus.COMPLETED.equals(downloadResult.getDownloadStatusStatus()));
    }

    @Test
    public void downloadFromSource_exception_opening_stream_test() throws IOException {

        when(fileDownloaderUtils.openStream(new URL("http://www.africau.edu/images/default/sample.pdf"))).thenThrow
                (new IOException());
        FileDownloadResult downloadResult = downloadManager.downloadFromSource("http://www.africau" +
                                                                                       ".edu/images/default/sample" +
                                                                                       ".pdf", "download");
        String saveLocation = fileDownloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                         ".edu/images/default/sample" +
                                                                                         ".pdf")
                , "download");

        Assert.assertFalse(Files.exists(Paths.get(saveLocation)));
        Assert.assertEquals(DownloadStatus.ERROR, downloadResult.getDownloadStatusStatus());
        Assert.assertEquals("RetryAttemptsExhausted", downloadResult.getMessage());
    }

    @Test
    public void downloadFromSource_malformed_url_string_test() throws MalformedURLException {

        FileDownloadResult downloadResult = downloadManager.downloadFromSource("abc.africau.edu/images/default/sample" +
                                                                                       ".pdf", "download");
        String saveLocation = fileDownloaderUtils.uniqueFileSaveLocation(new URL("http://www.africau" +
                                                                                         ".edu/images/default/sample" +
                                                                                         ".pdf")
                , "download");

        Assert.assertEquals(DownloadStatus.ERROR, downloadResult.getDownloadStatusStatus());
        Assert.assertEquals("MalformedURL", downloadResult.getMessage());

    }

    @Test
    public void downloadFromSource_non_recoverable_unknown_exception_test() throws IOException {

        String source = "http://www.africau" +
                ".edu/images/default/sample" +
                ".pdf";

        when(fileDownloaderUtils.openStream(new URL(source))).thenThrow
                (new RuntimeException("Unknown non recoverable Exception"));

        FileDownloadResult downloadResult = downloadManager.downloadFromSource(source, "download");

        Assert.assertEquals(DownloadStatus.ERROR, downloadResult.getDownloadStatusStatus());
        Assert.assertEquals("RetryAttemptsExhausted", downloadResult.getMessage());

    }

}
