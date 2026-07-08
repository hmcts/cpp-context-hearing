package uk.gov.moj.cpp.hearing.domain.event.result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.AffectedDefendant;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.AffectedOffence;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationErrors;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationIssue;

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

        final ValidationIssue error = new ValidationIssue()
                .ruleId("DR-SENT-002")
                .severity(ValidationIssue.SeverityEnum.ERROR)
                .affectedResultCodes(List.of())
                .affectedOffences(List.of(new AffectedOffence()
                        .offenceId("off-1")
                        .offenceTitle("Offence 1")
                        .message("Offences 1, 2 missing concurrent/consecutive info")))
                .affectedDefendants(List.of())
                .validationLevel(ValidationIssue.ValidationLevelEnum.OFFENCE);

        final ValidationIssue warning = new ValidationIssue()
                .ruleId("DR-SENT-002")
                .severity(ValidationIssue.SeverityEnum.WARNING)
                .affectedResultCodes(List.of())
                .affectedOffences(List.of())
                .affectedDefendants(List.of(new AffectedDefendant()
                        .defendantId("def-1")
                        .message("Offences show both concurrent and consecutive")))
                .validationLevel(ValidationIssue.ValidationLevelEnum.DEFENDANT);

        final ResultsValidationFailed result = ResultsValidationFailed.builder()
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withUserId(userId)
                .withValidationId("validation-abc")
                .withTimestamp("2026-03-16T10:00:00Z")
                .withMode("advisory")
                .withRulesEvaluated(List.of("DR-SENT-002"))
                .withIsValid(false)
                .withErrors(new ValidationErrors()
                        .errorMessages(List.of("Offences 1, 2 missing concurrent/consecutive info"))
                        .validationIssues(List.of(error)))
                .withWarnings(List.of(warning))
                .withProcessingTimeMs(42)
                .build();

        assertThat(result.getHearingId(), is(hearingId));
        assertThat(result.getHearingDay(), is(hearingDay));
        assertThat(result.getUserId(), is(userId));
        assertThat(result.getValidationId(), is("validation-abc"));
        assertThat(result.getTimestamp(), is("2026-03-16T10:00:00Z"));
        assertThat(result.getMode(), is("advisory"));
        assertThat(result.getRulesEvaluated(), hasSize(1));
        assertThat(result.isValid(), is(false));
        assertThat(result.getProcessingTimeMs(), is(42));
        assertThat(result.getErrors().getErrorMessages(), hasSize(1));
        assertThat(result.getErrors().getValidationIssues(), hasSize(1));
        assertThat(result.getErrors().getValidationIssues().get(0).getRuleId(), is("DR-SENT-002"));
        assertThat(result.getErrors().getValidationIssues().get(0).getSeverity(), is(ValidationIssue.SeverityEnum.ERROR));
        assertThat(result.getErrors().getValidationIssues().get(0).getAffectedOffences().get(0).getMessage(),
                is("Offences 1, 2 missing concurrent/consecutive info"));
        assertThat(result.getWarnings(), hasSize(1));
        assertThat(result.getWarnings().get(0).getSeverity(), is(ValidationIssue.SeverityEnum.WARNING));
        assertThat(result.getWarnings().get(0).getAffectedDefendants().get(0).getDefendantId(), is("def-1"));
    }

    @Test
    void shouldBuildWithEmptyErrorsAndWarnings() {
        final ResultsValidationFailed result = ResultsValidationFailed.builder()
                .withHearingId(UUID.randomUUID())
                .withHearingDay(LocalDate.now())
                .withUserId(UUID.randomUUID().toString())
                .withIsValid(true)
                .withErrors(new ValidationErrors().errorMessages(List.of()).validationIssues(List.of()))
                .withWarnings(List.of())
                .build();

        assertThat(result.getErrors().getErrorMessages(), is(empty()));
        assertThat(result.getErrors().getValidationIssues(), is(empty()));
        assertThat(result.getWarnings(), is(empty()));
    }

    @Test
    void shouldSerializeAndDeserialize() throws Exception {
        final UUID hearingId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.of(2026, 3, 16);
        final String userId = UUID.randomUUID().toString();

        final ValidationIssue error = new ValidationIssue()
                .ruleId("DR-SENT-002")
                .severity(ValidationIssue.SeverityEnum.ERROR)
                .affectedResultCodes(List.of())
                .affectedOffences(List.of(new AffectedOffence()
                        .offenceId("off-1")
                        .offenceTitle("Offence 1")
                        .message("Test message")))
                .affectedDefendants(List.of())
                .validationLevel(ValidationIssue.ValidationLevelEnum.OFFENCE);

        final ResultsValidationFailed original = ResultsValidationFailed.builder()
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withUserId(userId)
                .withValidationId("validation-abc")
                .withTimestamp("2026-03-16T10:00:00Z")
                .withMode("advisory")
                .withRulesEvaluated(List.of("DR-SENT-002"))
                .withIsValid(true)
                .withErrors(new ValidationErrors().errorMessages(List.of("Test message")).validationIssues(List.of(error)))
                .withWarnings(List.of())
                .withProcessingTimeMs(42)
                .build();

        final String json = objectMapper.writeValueAsString(original);

        // The boolean must serialize as the OpenAPI-contract key "isValid", not Jackson's default
        // is-prefix-stripped "valid". A missing "isValid" key deserializes to the boolean default (false),
        // which would silently mask a wrong key — so assert on the raw JSON, not just the round-trip.
        assertThat(json, containsString("\"isValid\""));
        assertThat(json, not(containsString("\"valid\"")));

        final ResultsValidationFailed deserialized = objectMapper.readValue(json, ResultsValidationFailed.class);

        assertThat(deserialized.getHearingId(), is(hearingId));
        assertThat(deserialized.getHearingDay(), is(hearingDay));
        assertThat(deserialized.getUserId(), is(userId));
        assertThat(deserialized.getValidationId(), is("validation-abc"));
        assertThat(deserialized.isValid(), is(true));
        assertThat(deserialized.getProcessingTimeMs(), is(42));
        assertThat(deserialized.getErrors().getErrorMessages(), hasSize(1));
        assertThat(deserialized.getErrors().getValidationIssues(), hasSize(1));
        assertThat(deserialized.getErrors().getValidationIssues().get(0).getRuleId(), is("DR-SENT-002"));
        assertThat(deserialized.getErrors().getValidationIssues().get(0).getAffectedOffences(), hasSize(1));
        assertThat(deserialized.getWarnings(), is(empty()));
    }
}
