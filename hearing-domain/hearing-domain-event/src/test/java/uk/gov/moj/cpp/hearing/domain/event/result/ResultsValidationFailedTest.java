package uk.gov.moj.cpp.hearing.domain.event.result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ResultsValidationFailedTest {

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Test
    void shouldHaveCorrectEventAnnotation() {
        final Event event = ResultsValidationFailed.class.getAnnotation(Event.class);
        assertThat(event, is(notNullValue()));
        assertThat(event.value(), is("hearing.events.results-validation-failed"));
    }

    @Test
    void shouldBuildWithAllFields() {
        final UUID hearingId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.of(2026, 3, 16);
        final String userId = UUID.randomUUID().toString();

        final ResultsValidationFailed.ValidationError error = new ResultsValidationFailed.ValidationError(
                "DR-SENT-002", "ERROR", "Offences 1, 2 missing concurrent/consecutive info",
                List.of("offence-1", "offence-2"));

        final ResultsValidationFailed.ValidationError warning = new ResultsValidationFailed.ValidationError(
                "DR-SENT-002", "WARNING", "Offences show both concurrent and consecutive",
                List.of("offence-3"));

        final ResultsValidationFailed result = ResultsValidationFailed.builder()
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withUserId(userId)
                .withErrors(List.of(error))
                .withWarnings(List.of(warning))
                .build();

        assertThat(result.getHearingId(), is(hearingId));
        assertThat(result.getHearingDay(), is(hearingDay));
        assertThat(result.getUserId(), is(userId));
        assertThat(result.getErrors(), hasSize(1));
        assertThat(result.getErrors().get(0).getRuleId(), is("DR-SENT-002"));
        assertThat(result.getErrors().get(0).getSeverity(), is("ERROR"));
        assertThat(result.getErrors().get(0).getMessage(), is("Offences 1, 2 missing concurrent/consecutive info"));
        assertThat(result.getErrors().get(0).getAffectedOffences(), hasSize(2));
        assertThat(result.getWarnings(), hasSize(1));
        assertThat(result.getWarnings().get(0).getSeverity(), is("WARNING"));
    }

    @Test
    void shouldBuildWithEmptyErrorsAndWarnings() {
        final ResultsValidationFailed result = ResultsValidationFailed.builder()
                .withHearingId(UUID.randomUUID())
                .withHearingDay(LocalDate.now())
                .withUserId(UUID.randomUUID().toString())
                .withErrors(List.of())
                .withWarnings(List.of())
                .build();

        assertThat(result.getErrors(), is(empty()));
        assertThat(result.getWarnings(), is(empty()));
    }

    @Test
    void shouldSerializeAndDeserialize() throws Exception {
        final UUID hearingId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.of(2026, 3, 16);
        final String userId = UUID.randomUUID().toString();

        final ResultsValidationFailed.ValidationError error = new ResultsValidationFailed.ValidationError(
                "DR-SENT-002", "ERROR", "Test message", List.of("offence-1"));

        final ResultsValidationFailed original = ResultsValidationFailed.builder()
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withUserId(userId)
                .withErrors(List.of(error))
                .withWarnings(List.of())
                .build();

        final String json = objectMapper.writeValueAsString(original);
        final ResultsValidationFailed deserialized = objectMapper.readValue(json, ResultsValidationFailed.class);

        assertThat(deserialized.getHearingId(), is(hearingId));
        assertThat(deserialized.getHearingDay(), is(hearingDay));
        assertThat(deserialized.getUserId(), is(userId));
        assertThat(deserialized.getErrors(), hasSize(1));
        assertThat(deserialized.getErrors().get(0).getRuleId(), is("DR-SENT-002"));
        assertThat(deserialized.getErrors().get(0).getAffectedOffences(), hasSize(1));
        assertThat(deserialized.getWarnings(), is(empty()));
    }
}
