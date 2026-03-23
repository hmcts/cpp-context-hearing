package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import uk.gov.justice.services.common.configuration.Value;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ResultsValidationClient implements ResultsValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsValidationClient.class);
    private static final String CJSCPPUID = "CJSCPPUID";

    @Inject
    @Value(key = "resultsvalidator.base.url", defaultValue = "http://localhost:8082/api/validation/validate")
    protected String validationUrl;

    @Inject
    @Value(key = "resultsvalidator.enabled", defaultValue = "true")
    protected String enabled;

    @Inject
    @Value(key = "resultsvalidator.timeout.ms", defaultValue = "5000")
    protected String timeoutMs;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private CloseableHttpClient httpClient;

    public ResultsValidationClient() {
    }

    public ValidationResponse validate(final ValidationRequest request, final String userId) {
        if (!"true".equalsIgnoreCase(enabled)) {
            LOGGER.debug("Results validation is disabled, proceeding with share");
            return ValidationResponse.passThrough();
        }

        try {
            final HttpPost httpPost = new HttpPost(validationUrl);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request), ContentType.APPLICATION_JSON));
            httpPost.addHeader(CJSCPPUID, userId);

            final HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode()) {
                try (final InputStream content = httpResponse.getEntity().getContent()) {
                    return objectMapper.readValue(content, ValidationResponse.class);
                }
            } else {
                LOGGER.error("Results validation service returned status {}, proceeding with share (fail-open)",
                        httpResponse.getStatusLine().getStatusCode());
                return ValidationResponse.passThrough();
            }
        } catch (final Exception ex) {
            LOGGER.error("Results validation service call failed, proceeding with share (fail-open)", ex);
            return ValidationResponse.passThrough();
        }
    }
}
