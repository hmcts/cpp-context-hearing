package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.core.featurecontrol.FeatureControlGuard;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationRequest;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationResponse;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationIssue;

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

    @Mock
    private FeatureControlGuard featureControlGuard;

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @BeforeEach
    void setUp() {
        setField(resultsValidationClient, "objectMapper", objectMapper);
        setField(resultsValidationClient, "validationUrl", "http://localhost:8082/api/validation/validate");
        setField(resultsValidationClient, "enabled", "true");
        setField(resultsValidationClient, "timeoutMs", "5000");
        lenient().when(featureControlGuard.isFeatureEnabled("ResultsValidation")).thenReturn(true);
    }

    @Test
    void shouldReturnValidResponseWhenServiceReturns200WithNoErrors() throws Exception {
        final String responseJson = """
                {"validationId":"abc","isValid":true,"errors":{"errorMessages":[],"validationIssues":[]},"warnings":[],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
    }

    @Test
    void shouldReturnErrorsWhenServiceReturns200WithErrors() throws Exception {
        final String responseJson = """
                {"validationId":"abc","isValid":false,"errors":{"errorMessages":["Missing info"],"validationIssues":[{"ruleId":"DR-SENT-002","severity":"ERROR","affectedResultCodes":[],"affectedOffences":[{"offenceId":"off-1","offenceTitle":"Offence 1","message":"Missing info"}],"affectedDefendants":[],"validationLevel":"OFFENCE"}]},"warnings":[],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(false));
        assertThat(response.getErrors().getValidationIssues(), hasSize(1));
        final ValidationIssue issue = response.getErrors().getValidationIssues().get(0);
        assertThat(issue.getRuleId(), is("DR-SENT-002"));
        assertThat(issue.getSeverity(), is(ValidationIssue.SeverityEnum.ERROR));
        assertThat(issue.getValidationLevel(), is(ValidationIssue.ValidationLevelEnum.OFFENCE));
        assertThat(issue.getAffectedOffences().get(0).getOffenceId(), is("off-1"));
        assertThat(issue.getAffectedOffences().get(0).getMessage(), is("Missing info"));
    }

    @Test
    void shouldReturnNoErrorsWhenServiceReturns200WithWarningsOnly() throws Exception {
        final String responseJson = """
                {"validationId":"abc","isValid":true,"errors":{"errorMessages":[],"validationIssues":[]},"warnings":[{"ruleId":"DR-SENT-002","severity":"WARNING","affectedResultCodes":[],"affectedOffences":[],"affectedDefendants":[{"defendantId":"def-1","message":"Both concurrent and consecutive"}],"validationLevel":"DEFENDANT"}],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
        assertThat(response.getWarnings(), hasSize(1));
        assertThat(response.getWarnings().get(0).getSeverity(), is(ValidationIssue.SeverityEnum.WARNING));
        assertThat(response.getWarnings().get(0).getAffectedDefendants().get(0).getDefendantId(), is("def-1"));
    }

    @Test
    void shouldReturnPassThroughWhenServiceThrowsIOException() throws Exception {
        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("Connection refused"));

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
        assertThat(response.getErrors().getValidationIssues(), is(empty()));
    }

    @Test
    void shouldReturnPassThroughWhenServiceReturnsNon200Status() throws Exception {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);

        when(statusLine.getStatusCode()).thenReturn(500);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
    }

    @Test
    void shouldReturnPassThroughWithoutHttpCallWhenDisabled() throws Exception {
        setField(resultsValidationClient, "enabled", "false");

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
        verify(httpClient, never()).execute(any());
    }

    @Test
    void toggle_off_returns_passThrough_without_http_call() throws Exception {
        when(featureControlGuard.isFeatureEnabled("ResultsValidation")).thenReturn(false);

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
        verify(httpClient, never()).execute(any());
    }

    @Test
    void toggle_on_invokes_http_client() throws Exception {
        final String responseJson = """
                {"validationId":"abc","isValid":true,"errors":{"errorMessages":[],"validationIssues":[]},"warnings":[],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
        verify(httpClient).execute(any(HttpPost.class));
    }

    @Test
    void toggle_lookup_failure_falls_open_and_invokes_http_client() throws Exception {
        when(featureControlGuard.isFeatureEnabled("ResultsValidation")).thenThrow(new RuntimeException("feature store unavailable"));
        final String responseJson = """
                {"validationId":"abc","isValid":true,"errors":{"errorMessages":[],"validationIssues":[]},"warnings":[],"rulesEvaluated":["DR-SENT-002"],"processingTimeMs":10}
                """;
        mockHttpResponse(200, responseJson);

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
        verify(httpClient).execute(any(HttpPost.class));
    }

    @Test
    void existing_static_disabled_path_still_short_circuits() throws Exception {
        setField(resultsValidationClient, "enabled", "false");

        final DraftValidationResponse response = resultsValidationClient.validate(buildRequest(), "user-123");

        assertThat(response.getIsValid(), is(true));
        verify(httpClient, never()).execute(any());
    }

    @Test
    void isShareBlockingEnabled_falseWhenBlank() {
        setField(resultsValidationClient, "shareBlocking", "");

        assertThat(resultsValidationClient.isShareBlockingEnabled(), is(false));
    }

    @Test
    void isShareBlockingEnabled_falseWhenDisabled() {
        setField(resultsValidationClient, "shareBlocking", "disabled");

        assertThat(resultsValidationClient.isShareBlockingEnabled(), is(false));
    }

    @Test
    void isShareBlockingEnabled_trueWhenEnabled() {
        setField(resultsValidationClient, "shareBlocking", "enabled");

        assertThat(resultsValidationClient.isShareBlockingEnabled(), is(true));
    }

    @Test
    void isShareBlockingEnabled_trueWhenEnabledIgnoringCaseAndWhitespace() {
        setField(resultsValidationClient, "shareBlocking", "  ENABLED  ");

        assertThat(resultsValidationClient.isShareBlockingEnabled(), is(true));
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

    private DraftValidationRequest buildRequest() {
        return new DraftValidationRequest()
                .hearingId("hearing-1")
                .hearingDay(LocalDate.of(2026, 3, 16))
                .courtType(DraftValidationRequest.CourtTypeEnum.MAGISTRATES)
                .resultLines(List.of())
                .offences(List.of())
                .defendants(List.of());
    }
}
