package com.abhishekjain.filedownloader.configuration.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

/**
 * Cross cutting class to log the retry attempts for downloading the file
 */
public class RetryDownloadListener extends RetryListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(RetryDownloadListener.class);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context,
                                                 RetryCallback<T, E> callback, Throwable throwable) {

        log.info("Error occurred while downloading from source. Hence retrying to download. Exception: {}", throwable
                .getMessage());
        super.onError(context, callback, throwable);
    }
}
