package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpClientProducerTest {

    @Spy
    @InjectMocks
    private HttpClientProducer httpClientProducer;

    @Mock
    private HttpClient mockHttpClient;

    @BeforeEach
    void setUp() {
        setField(httpClientProducer, "client", null);
        setField(httpClientProducer, "socketTimeoutMs", "5000");
        setField(httpClientProducer, "connectTimeoutMs", "1000");
        setField(httpClientProducer, "connectionRequestTimeoutMs", "500");
        setField(httpClientProducer, "poolMaxTotal", "200");
        setField(httpClientProducer, "poolMaxPerRoute", "100");
        setField(httpClientProducer, "evictIdleSeconds", "30");
    }

    @Test
    void createHttpClient_shouldReturnNonNullHttpClient() {
        doReturn(mockHttpClient).when(httpClientProducer).buildClient(any(PoolingHttpClientConnectionManager.class), any(RequestConfig.class));

        final HttpClient client = httpClientProducer.createHttpClient();

        assertThat(client, is(notNullValue()));
    }

    @Test
    void createHttpClient_shouldReturnHttpClient() {
        doReturn(mockHttpClient).when(httpClientProducer).buildClient(any(PoolingHttpClientConnectionManager.class), any(RequestConfig.class));

        final HttpClient client = httpClientProducer.createHttpClient();

        assertThat(client, instanceOf(HttpClient.class));
    }

    @Test
    void createHttpClient_shouldReturnHttpClient_withCustomValues() {
        setField(httpClientProducer, "socketTimeoutMs", "3000");
        setField(httpClientProducer, "connectTimeoutMs", "2000");
        setField(httpClientProducer, "connectionRequestTimeoutMs", "750");
        setField(httpClientProducer, "poolMaxTotal", "50");
        setField(httpClientProducer, "poolMaxPerRoute", "25");
        setField(httpClientProducer, "evictIdleSeconds", "60");
        doReturn(mockHttpClient).when(httpClientProducer).buildClient(any(PoolingHttpClientConnectionManager.class), any(RequestConfig.class));

        final HttpClient client = httpClientProducer.createHttpClient();

        assertThat(client, is(notNullValue()));
    }

    @Test
    void close_whenClientIsNotNull_shouldCloseClient() throws IOException {
        final Closeable closeableClient = mock(Closeable.class);
        setField(httpClientProducer, "client", closeableClient);

        httpClientProducer.close();

        verify(closeableClient).close();
    }

    @Test
    void close_whenClientIsNull_shouldNotThrow() {
        assertDoesNotThrow(() -> httpClientProducer.close());
    }

    @Test
    void close_whenClientThrowsException_shouldNotPropagate() throws IOException {
        final Closeable closeableClient = mock(Closeable.class);
        setField(httpClientProducer, "client", closeableClient);
        doThrow(new IOException("connection reset")).when(closeableClient).close();

        httpClientProducer.close();

        verify(closeableClient).close();
    }
}
