package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ValidationResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Test
    void shouldDeserializeValidResponse() throws Exception {
        final String json = """
                {
                  "validationId": "abc-123",
                  "isValid": true,
                  "errors": [],
                  "warnings": [],
                  "rulesEvaluated": ["DR-SENT-002"],
                  "processingTimeMs": 42
                }
                """;

        final ValidationResponse response = objectMapper.readValue(json, ValidationResponse.class);

        assertThat(response.isValid(), is(true));
        assertThat(response.getErrors(), is(empty()));
        assertThat(response.getWarnings(), is(empty()));
    }

    @Test
    void shouldDeserializeResponseWithErrors() throws Exception {
        final String json = """
                {
                  "validationId": "abc-123",
                  "isValid": false,
                  "errors": [
                    {
                      "ruleId": "DR-SENT-002",
                      "severity": "ERROR",
                      "message": "Offences 1, 2 missing info",
                      "affectedOffences": [
                        {"id": "off-1", "title": "Offence 1"}
                      ]
                    }
                  ],
                  "warnings": [],
                  "rulesEvaluated": ["DR-SENT-002"],
                  "processingTimeMs": 42
                }
                """;

        final ValidationResponse response = objectMapper.readValue(json, ValidationResponse.class);

        assertThat(response.isValid(), is(false));
        assertThat(response.hasErrors(), is(true));
        assertThat(response.getErrors(), hasSize(1));
        assertThat(response.getErrors().get(0).getRuleId(), is("DR-SENT-002"));
        assertThat(response.getErrors().get(0).getSeverity(), is("ERROR"));
        assertThat(response.getErrors().get(0).getMessage(), is("Offences 1, 2 missing info"));
    }

    @Test
    void shouldReturnHasErrorsFalseWhenOnlyWarnings() throws Exception {
        final String json = """
                {
                  "validationId": "abc-123",
                  "isValid": true,
                  "errors": [],
                  "warnings": [
                    {
                      "ruleId": "DR-SENT-002",
                      "severity": "WARNING",
                      "message": "Both concurrent and consecutive",
                      "affectedOffences": []
                    }
                  ],
                  "rulesEvaluated": ["DR-SENT-002"],
                  "processingTimeMs": 42
                }
                """;

        final ValidationResponse response = objectMapper.readValue(json, ValidationResponse.class);

        assertThat(response.hasErrors(), is(false));
        assertThat(response.getWarnings(), hasSize(1));
    }

    @Test
    void shouldCreatePassThroughResponse() {
        final ValidationResponse passThrough = ValidationResponse.passThrough();

        assertThat(passThrough.isValid(), is(true));
        assertThat(passThrough.hasErrors(), is(false));
        assertThat(passThrough.getErrors(), is(empty()));
        assertThat(passThrough.getWarnings(), is(empty()));
    }
}
