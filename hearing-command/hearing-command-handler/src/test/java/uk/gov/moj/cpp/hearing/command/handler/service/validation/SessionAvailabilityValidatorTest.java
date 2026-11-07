package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
import static uk.gov.moj.cpp.hearing.command.handler.service.validation.SessionAvailabilityValidator.EXPIRED_MESSAGE;
import static uk.gov.moj.cpp.hearing.command.handler.service.validation.SessionAvailabilityValidator.NOT_AVAILABLE_MESSAGE;
import static uk.gov.moj.cpp.hearing.command.handler.service.validation.SessionAvailabilityValidator.NOT_CHECKED_MESSAGE;
import static uk.gov.moj.cpp.hearing.command.handler.service.validation.SessionAvailabilityValidator.RULE_ID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationErrors;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationIssue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionAvailabilityValidatorTest {

    private static final UUID SYSTEM_USER = randomUUID();

    @InjectMocks
    private SessionAvailabilityValidator validator;

    @Mock
    private HttpClient httpClient;

    @Mock
    private SystemUserProvider systemUserProvider;

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    private Hearing hearing;
    private Offence offence;

    @BeforeEach
    void setUp() {
        setField(validator, "objectMapper", objectMapper);
        setField(validator, "baseUri", "http://courtscheduler/rest/courtscheduler");
        lenient().when(systemUserProvider.getContextSystemUserId()).thenReturn(Optional.of(SYSTEM_USER));
        hearing = standardInitiateHearingTemplate().getHearing();
        offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
    }

    @Test
    void asksNothingWhenNoLineCarriesABooking() throws Exception {
        final ValidationErrors errors = validator.validate(List.of(offenceLine(offence.getId(), null)), hearing);

        assertThat(SessionAvailabilityValidator.isEmpty(errors), is(true));
        verify(httpClient, never()).execute(any());
    }

    // STE15, 2026-10-27, hearing 5c7b38f0: a share merged two lines and released 4b778972 as
    // surplus; the amended line still carried it. Placeable - it must not block the share.
    @Test
    void reportsNothingForBookingsThatAreSafeToShare() throws Exception {
        answers("""
                {"bookings":[
                  {"bookingId":"bk-held","safeToShare":true,"status":"RESERVED"},
                  {"bookingId":"4b778972","safeToShare":true,"status":"SURPLUS"}]}""");

        final ValidationErrors errors = validator.validate(
                List.of(offenceLine(offence.getId(), "bk-held"), offenceLine(randomUUID(), "4b778972")), hearing);

        assertThat(SessionAvailabilityValidator.isEmpty(errors), is(true));
    }

    @Test
    void asksAsTheSystemUserAndReBooksAnExpiredHold() throws Exception {
        answers("{\"bookings\":[{\"bookingId\":\"bk-1\",\"safeToShare\":true,\"status\":\"REBOOKED\"}]}");

        validator.validate(List.of(offenceLine(offence.getId(), "bk-1")), hearing);

        final ArgumentCaptor<HttpPost> post = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(post.capture());
        assertThat(post.getValue().getURI().toString(), is("http://courtscheduler/rest/courtscheduler/unconfirmedBooking/status"));
        assertThat(post.getValue().getFirstHeader("Content-Type").getValue(), is("application/vnd.courtscheduler.query.booking.status+json"));
        assertThat(post.getValue().getFirstHeader("CJSCPPUID").getValue(), is(SYSTEM_USER.toString()));
        final JsonNode body = objectMapper.readTree(EntityUtils.toString(post.getValue().getEntity()));
        assertThat(body.path("bookingIds").get(0).asText(), is("bk-1"));
        assertThat(body.path("reserveIfExpired").asBoolean(), is(true));
    }

    @Test
    void reportsAnExpiredSessionAgainstTheOffenceThatPickedIt() throws Exception {
        answers("{\"bookings\":[{\"bookingId\":\"bk-1\",\"safeToShare\":false,\"status\":\"EXPIRED\"}]}");

        final ValidationErrors errors = validator.validate(List.of(offenceLine(offence.getId(), "bk-1")), hearing);

        assertThat(errors.getValidationIssues(), hasSize(1));
        final ValidationIssue issue = errors.getValidationIssues().get(0);
        assertThat(issue.getRuleId(), is(RULE_ID));
        assertThat(issue.getSeverity(), is(ValidationIssue.SeverityEnum.ERROR));
        assertThat(issue.getValidationLevel(), is(ValidationIssue.ValidationLevelEnum.OFFENCE));
        assertThat(issue.getAffectedResultCodes(), contains("NHMC"));
        assertThat(issue.getAffectedOffences(), hasSize(1));
        assertThat(issue.getAffectedOffences().get(0).getOffenceId(), is(offence.getId().toString()));
        assertThat(issue.getAffectedOffences().get(0).getOffenceTitle(), is(offence.getOffenceTitle()));
        assertThat(issue.getAffectedOffences().get(0).getMessage(), is(EXPIRED_MESSAGE));
        assertThat(errors.getErrorMessages(), is(empty()));
    }

    @Test
    void tellsAReleasedSessionFromAnExpiredOne() throws Exception {
        answers("{\"bookings\":[{\"bookingId\":\"bk-1\",\"safeToShare\":false,\"status\":\"RELEASED\"}]}");

        final ValidationErrors errors = validator.validate(List.of(offenceLine(offence.getId(), "bk-1")), hearing);

        assertThat(errors.getValidationIssues().get(0).getAffectedOffences().get(0).getMessage(), is(NOT_AVAILABLE_MESSAGE));
    }

    @Test
    void reportsEveryOffenceCarryingTheBlockedBooking() throws Exception {
        answers("{\"bookings\":[{\"bookingId\":\"bk-1\",\"safeToShare\":false,\"status\":\"EXPIRED\"}]}");
        final UUID otherOffence = randomUUID();

        final ValidationErrors errors = validator.validate(
                List.of(offenceLine(offence.getId(), "bk-1"), offenceLine(otherOffence, "bk-1")), hearing);

        assertThat(errors.getValidationIssues().get(0).getAffectedOffences().stream().map(o -> o.getOffenceId()).toList(),
                containsInAnyOrder(offence.getId().toString(), otherOffence.toString()));
    }

    // Decision: courtscheduler unreachable blocks the share.
    @Test
    void blocksEveryBookedLineWhenCourtschedulerCannotBeReached() throws Exception {
        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("Connection refused"));

        final ValidationErrors errors = validator.validate(List.of(offenceLine(offence.getId(), "bk-1")), hearing);

        assertThat(errors.getValidationIssues().get(0).getAffectedOffences().get(0).getMessage(), is(NOT_CHECKED_MESSAGE));
    }

    @Test
    void blocksWhenCourtschedulerAnswersWithAnError() throws Exception {
        answers(503, "");

        final ValidationErrors errors = validator.validate(List.of(offenceLine(offence.getId(), "bk-1")), hearing);

        assertThat(errors.getValidationIssues().get(0).getAffectedOffences().get(0).getMessage(), is(NOT_CHECKED_MESSAGE));
    }

    @Test
    void blocksABookingCourtschedulerLeftOutOfItsAnswer() throws Exception {
        answers("{\"bookings\":[]}");

        final ValidationErrors errors = validator.validate(List.of(offenceLine(offence.getId(), "bk-1")), hearing);

        assertThat(errors.getValidationIssues().get(0).getAffectedOffences().get(0).getMessage(), is(NOT_CHECKED_MESSAGE));
    }

    // Lines without an offence are reported where the results validator reports them.
    @Test
    void reportsADefendantLevelLineAgainstTheDefendant() throws Exception {
        answers("{\"bookings\":[{\"bookingId\":\"bk-1\",\"safeToShare\":false,\"status\":\"EXPIRED\"}]}");
        final UUID defendantId = randomUUID();

        final ValidationErrors errors = validator.validate(List.of(line(null, defendantId, "bk-1")), hearing);

        final ValidationIssue issue = errors.getValidationIssues().get(0);
        assertThat(issue.getValidationLevel(), is(ValidationIssue.ValidationLevelEnum.DEFENDANT));
        assertThat(issue.getAffectedDefendants().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(issue.getAffectedDefendants().get(0).getMessage(), is(EXPIRED_MESSAGE));
    }

    @Test
    void reportsALineWithNeitherOffenceNorDefendantAsAnErrorMessage() throws Exception {
        answers("{\"bookings\":[{\"bookingId\":\"bk-1\",\"safeToShare\":false,\"status\":\"EXPIRED\"}]}");

        final ValidationErrors errors = validator.validate(List.of(line(null, null, "bk-1")), hearing);

        assertThat(errors.getValidationIssues(), is(empty()));
        assertThat(errors.getErrorMessages(), contains(EXPIRED_MESSAGE));
    }

    @Test
    void ignoresDeletedLines() throws Exception {
        final SharedResultsCommandResultLineV2 deleted = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("NHMC")
                .withOffenceId(offence.getId())
                .withIsDeleted(true)
                .withPrompts(List.of(bookingPrompt("bk-1")))
                .build();

        final ValidationErrors errors = validator.validate(List.of(deleted), hearing);

        assertThat(SessionAvailabilityValidator.isEmpty(errors), is(true));
        verify(httpClient, never()).execute(any());
    }

    private void answers(final String body) throws IOException {
        answers(200, body);
    }

    private void answers(final int status, final String body) throws IOException {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        final HttpEntity entity = mock(HttpEntity.class);
        when(statusLine.getStatusCode()).thenReturn(status);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        lenient().when(httpResponse.getEntity()).thenReturn(entity);
        lenient().when(entity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
    }

    private static SharedResultsCommandResultLineV2 offenceLine(final UUID offenceId, final String bookingId) {
        return line(offenceId, null, bookingId);
    }

    private static SharedResultsCommandResultLineV2 line(final UUID offenceId, final UUID defendantId, final String bookingId) {
        return SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("NHMC")
                .withOffenceId(offenceId)
                .withDefendantId(defendantId)
                .withPrompts(bookingId == null ? List.of() : List.of(bookingPrompt(bookingId)))
                .build();
    }

    private static SharedResultsCommandPrompt bookingPrompt(final String bookingId) {
        return new SharedResultsCommandPrompt(randomUUID(), "Booking reference", null, bookingId, null, null, "bookingReference");
    }
}
