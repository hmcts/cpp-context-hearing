package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

@ExtendWith(MockitoExtension.class)
class ResultsValidationClientTest {

    @InjectMocks
    private ResultsValidationClient resultsValidationClient;

    @Mock
    private HttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @BeforeEach
    void setUp() {
        setField(resultsValidationClient, "objectMapper", objectMapper);
        setField(resultsValidationClient, "validationUrl", "http://localhost:8082/api/validation/validate");
        setField(resultsValidationClient, "enabled", "true");
        setField(resultsValidationClient, "timeoutMs", "5000");
    }

    @Test
    void shouldReturnValidResponseWhenServiceReturns200WithNoErrors() throws Exception {
        final String responseJson = """
                {"validationId":"abc","isValid":true,"errors":[],"warnings":[],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final ValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.isValid(), is(true));
        assertThat(response.hasErrors(), is(false));
    }

    @Test
    void shouldReturnErrorsWhenServiceReturns200WithErrors() throws Exception {
        final String responseJson = """
                {"validationId":"abc","isValid":false,"errors":[{"ruleId":"DR-SENT-002","severity":"ERROR","message":"Missing info","affectedOffences":[]}],"warnings":[],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final ValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.isValid(), is(false));
        assertThat(response.hasErrors(), is(true));
        assertThat(response.getErrors(), hasSize(1));
    }

    @Test
    void shouldReturnNoErrorsWhenServiceReturns200WithWarningsOnly() throws Exception {
        final String responseJson = """
                {"validationId":"abc","isValid":true,"errors":[],"warnings":[{"ruleId":"DR-SENT-002","severity":"WARNING","message":"Advisory","affectedOffences":[]}],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final ValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.hasErrors(), is(false));
        assertThat(response.getWarnings(), hasSize(1));
    }

    @Test
    void shouldReturnPassThroughWhenServiceThrowsIOException() throws Exception {
        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("Connection refused"));

        final ValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.isValid(), is(true));
        assertThat(response.hasErrors(), is(false));
        assertThat(response.getErrors(), is(empty()));
    }

    @Test
    void shouldReturnPassThroughWhenServiceReturnsNon200Status() throws Exception {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);

        when(statusLine.getStatusCode()).thenReturn(500);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

        final ValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.isValid(), is(true));
        assertThat(response.hasErrors(), is(false));
    }

    @Test
    void shouldReturnPassThroughWithoutHttpCallWhenDisabled() throws Exception {
        setField(resultsValidationClient, "enabled", "false");

        final ValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.isValid(), is(true));
        assertThat(response.hasErrors(), is(false));
        verify(httpClient, never()).execute(any());
    }

    private void mockHttpResponse(final int statusCode, final String body) throws IOException {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        final HttpEntity entity = mock(HttpEntity.class);

        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
    }

    private ValidationRequest buildRequest() {
        return new ValidationRequest(
                "hearing-1",
                LocalDate.of(2026, 3, 16),
                "MAGISTRATES",
                null,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
