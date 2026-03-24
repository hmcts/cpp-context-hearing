package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import uk.gov.justice.services.common.configuration.Value;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HttpClientProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientProducer.class);

    @Inject
    @Value(key = "resultsvalidator.timeout.ms", defaultValue = "5000")
    private String DEFAULT_TIMEOUT_MS;

    private CloseableHttpClient client;

    @Produces
    @ApplicationScoped
    public HttpClient createHttpClient() {
        int connectTimeout = Integer.parseInt(DEFAULT_TIMEOUT_MS);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectTimeout)
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
