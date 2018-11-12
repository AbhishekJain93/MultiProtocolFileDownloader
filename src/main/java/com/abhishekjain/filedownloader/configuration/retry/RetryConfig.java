package com.abhishekjain.filedownloader.configuration.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class to register retry and backoff policy for retry template {@link RetryTemplate}
 */
@Configuration
class RetryConfig {

    @Value("${download.retry.count}")
    private int maxRetryAttempts;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.registerListener(new RetryDownloadListener());

        /* Exponential Backoff Strategy can also be used.{@link ExponentialBackOffPolicy}.
        A common use case is to
        backoff with an exponentially increasing wait period, to avoid two retries getting into lock step */
        
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000l);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        Map<Class<? extends Throwable>, Boolean> exceptionClassifier = new HashMap<>();
        exceptionClassifier.put(IOException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxRetryAttempts, exceptionClassifier);

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

}

