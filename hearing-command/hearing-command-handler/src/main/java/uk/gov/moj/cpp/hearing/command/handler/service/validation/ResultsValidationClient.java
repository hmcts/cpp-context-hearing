package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.core.featurecontrol.FeatureControlGuard;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationRequest;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationResponse;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationErrors;

import java.io.InputStream;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ResultsValidationClient implements ResultsValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsValidationClient.class);
    private static final String CJSCPPUID = "CJSCPPUID";

    @Inject
    @Value(key = "resultsvalidator.base.url", defaultValue = "http://localhost:8080/results-validator/api/validation/validate")
    protected String validationUrl;

    @Inject
    @Value(key = "resultsvalidator.enabled", defaultValue = "true")
    protected String enabled;

    @Inject
    @Value(key = "resultsvalidator.timeout.ms", defaultValue = "5000")
    protected String timeoutMs;

    @Inject
    @Value(key = "resultsvalidator.share.blocking", defaultValue = "disabled")
    protected String shareBlocking;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private HttpClient httpClient;

    @Inject
    private FeatureControlGuard featureControlGuard;

    public ResultsValidationClient() {
    }

    @Override
    public DraftValidationResponse validate(final DraftValidationRequest request, final String userId) {
        try {
            if (!featureControlGuard.isFeatureEnabled("ResultsValidation")) {
                LOGGER.debug("ResultsValidation feature toggle is OFF, skipping validation");
                return passThrough();
            }
        } catch (final Exception ex) {
            LOGGER.warn("ResultsValidation feature toggle lookup failed, proceeding with validation (fail-open)", ex);
        }

        if (!"true".equalsIgnoreCase(enabled)) {
            LOGGER.debug("Results validation is disabled, proceeding with share");
            return passThrough();
        }

        try {
            final HttpPost httpPost = new HttpPost(validationUrl);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request), ContentType.APPLICATION_JSON));
            httpPost.addHeader(CJSCPPUID, userId);

            final HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode()) {
                try (final InputStream content = httpResponse.getEntity().getContent()) {
                    return objectMapper.readValue(content, DraftValidationResponse.class);
                }
            } else {
                LOGGER.error("Results validation service returned status {}, proceeding with share (fail-open)",
                        httpResponse.getStatusLine().getStatusCode());
                return passThrough();
            }
        } catch (final Exception ex) {
            LOGGER.error("Results validation service call failed, proceeding with share (fail-open)", ex);
            return passThrough();
        }
    }

    @Override
    public boolean isShareBlockingEnabled() {
        return "enabled".equalsIgnoreCase(shareBlocking.trim());
    }

    /**
     * Builds a "pass-through" response used whenever validation is skipped or the validation service is
     * unreachable (fail-open): valid, no errors, no warnings.
     */
    private static DraftValidationResponse passThrough() {
        return new DraftValidationResponse()
                .mode("pass-through")
                .isValid(true)
                .rulesEvaluated(List.of())
                .errors(new ValidationErrors().errorMessages(List.of()).validationIssues(List.of()))
                .warnings(List.of());
    }
}
