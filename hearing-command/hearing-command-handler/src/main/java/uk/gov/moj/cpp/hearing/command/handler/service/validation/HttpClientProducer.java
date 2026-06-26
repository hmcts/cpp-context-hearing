package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import uk.gov.justice.services.common.configuration.Value;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HttpClientProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientProducer.class);

    @Inject
    @Value(key = "resultsvalidator.timeout.ms", defaultValue = "5000")
    private String socketTimeoutMs;

    @Inject
    @Value(key = "resultsvalidator.timeout.connect.ms", defaultValue = "1000")
    private String connectTimeoutMs;

    @Inject
    @Value(key = "resultsvalidator.timeout.connection.request.ms", defaultValue = "1000")
    private String connectionRequestTimeoutMs;

    @Inject
    @Value(key = "resultsvalidator.pool.max.total", defaultValue = "400")
    private String poolMaxTotal;

    @Inject
    @Value(key = "resultsvalidator.pool.max.per.route", defaultValue = "200")
    private String poolMaxPerRoute;

    @Inject
    @Value(key = "resultsvalidator.evict.idle.seconds", defaultValue = "30")
    private String evictIdleSeconds;

    private Closeable client;

    @Produces
    @ApplicationScoped
    public HttpClient createHttpClient() {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(Integer.parseInt(poolMaxTotal));
        connectionManager.setDefaultMaxPerRoute(Integer.parseInt(poolMaxPerRoute));

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Integer.parseInt(connectTimeoutMs))
                .setSocketTimeout(Integer.parseInt(socketTimeoutMs))
                .setConnectionRequestTimeout(Integer.parseInt(connectionRequestTimeoutMs))
                .build();

        final CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(Long.parseLong(evictIdleSeconds), TimeUnit.SECONDS)
                .evictExpiredConnections()
                .build();
        client = httpClient;
        return httpClient;
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
