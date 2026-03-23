package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HttpClientProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientProducer.class);
    private static final int DEFAULT_TIMEOUT_MS = 5000;

    private CloseableHttpClient client;

    @Produces
    @ApplicationScoped
    public CloseableHttpClient createHttpClient() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_TIMEOUT_MS)
                .setSocketTimeout(DEFAULT_TIMEOUT_MS)
                .setConnectionRequestTimeout(DEFAULT_TIMEOUT_MS)
                .build();

        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        return client;
    }

    @PreDestroy
    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (final Exception e) {
                LOGGER.warn("Failed to close HttpClient", e);
            }
        }
    }
}
