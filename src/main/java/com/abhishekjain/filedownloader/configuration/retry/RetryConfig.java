package com.abhishekjain.filedownloader.configuration.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
class RetryConfig {

    @Value("${download.retry.count}")
    private int maxRetryAttempts;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.registerListener(new RetryDownloadListener());

        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setMultiplier(2.0D);
        exponentialBackOffPolicy.setInitialInterval(100L);
        exponentialBackOffPolicy.setMaxInterval(30000L);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        Map<Class<? extends Throwable>, Boolean> exceptionClassifier = new HashMap<>();
        exceptionClassifier.put(IOException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxRetryAttempts, exceptionClassifier);

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

}
