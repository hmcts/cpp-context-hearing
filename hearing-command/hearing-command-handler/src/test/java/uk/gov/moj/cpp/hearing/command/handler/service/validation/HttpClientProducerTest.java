package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpClientProducerTest {

    private static class FakeCloseableHttpClient extends CloseableHttpClient {
        boolean closeCalled = false;
        boolean throwOnClose = false;

        @Override
        protected CloseableHttpResponse doExecute(final HttpHost target, final HttpRequest request, final HttpContext context) {
            return null;
        }

        @Override
        @Deprecated
        public ClientConnectionManager getConnectionManager() {
            return null;
        }

        @Override
        @Deprecated
        public org.apache.http.params.HttpParams getParams() {
            return null;
        }

        @Override
        public void close() throws IOException {
            closeCalled = true;
            if (throwOnClose) {
                throw new IOException("connection reset");
            }
        }
    }

    @InjectMocks
    private HttpClientProducer httpClientProducer;

    private FakeCloseableHttpClient closeableHttpClient;

    @BeforeEach
    void setUp() {
        closeableHttpClient = new FakeCloseableHttpClient();
        setField(httpClientProducer, "socketTimeoutMs", "5000");
        setField(httpClientProducer, "connectTimeoutMs", "1000");
        setField(httpClientProducer, "connectionRequestTimeoutMs", "500");
        setField(httpClientProducer, "poolMaxTotal", "200");
        setField(httpClientProducer, "poolMaxPerRoute", "100");
        setField(httpClientProducer, "evictIdleSeconds", "30");
    }

    @Test
    void createHttpClient_shouldReturnNonNullHttpClient() {
        final HttpClient client = httpClientProducer.createHttpClient();

        assertThat(client, is(notNullValue()));
    }

    @Test
    void createHttpClient_shouldReturnCloseableHttpClient() {
        final HttpClient client = httpClientProducer.createHttpClient();

        assertThat(client, instanceOf(CloseableHttpClient.class));
    }

    @Test
    void createHttpClient_shouldReturnHttpClient_withCustomValues() {
        setField(httpClientProducer, "socketTimeoutMs", "3000");
        setField(httpClientProducer, "connectTimeoutMs", "2000");
        setField(httpClientProducer, "connectionRequestTimeoutMs", "750");
        setField(httpClientProducer, "poolMaxTotal", "50");
        setField(httpClientProducer, "poolMaxPerRoute", "25");
        setField(httpClientProducer, "evictIdleSeconds", "60");

        final HttpClient client = httpClientProducer.createHttpClient();

        assertThat(client, is(notNullValue()));
    }

    @Test
    void close_whenClientIsNotNull_shouldCloseClient() {
        setField(httpClientProducer, "client", closeableHttpClient);

        httpClientProducer.close();

        assertThat(closeableHttpClient.closeCalled, is(true));
    }

    @Test
    void close_whenClientIsNull_shouldNotThrow() {
        assertDoesNotThrow(() -> httpClientProducer.close());
    }

    @Test
    void close_whenClientThrowsException_shouldNotPropagate() {
        setField(httpClientProducer, "client", closeableHttpClient);
        closeableHttpClient.throwOnClose = true;

        httpClientProducer.close();

        assertThat(closeableHttpClient.closeCalled, is(true));
    }
}
