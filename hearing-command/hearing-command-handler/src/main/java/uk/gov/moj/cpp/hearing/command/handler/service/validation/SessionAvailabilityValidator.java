package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.AffectedDefendant;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.AffectedOffence;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationErrors;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ValidationIssue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks, at share, that every session the result lines picked is still held, and reports each one
 * that is not as a validation issue against the offences that carry it - the same shape the results
 * validator answers in, so the share is blocked, or not, exactly as for any other validation error.
 *
 * <p>Runs in the share and only there, as results validation does: a hold can lapse between an
 * amendment being requested, approved and shared, so only a check at the share itself is reliable.
 *
 * <p>Asks courtscheduler with {@code reserveIfExpired}, so a hold that lapsed is re-reserved on the
 * same session if it still has room - at every share, however often it lapses. Every booking the
 * lines carry is asked about, shared ones included: courtscheduler answers those SHARED, SURPLUS or
 * UNLISTED, all safe to share, so no line needs filtering out first.
 *
 * <p><b>Fails closed.</b> If courtscheduler cannot answer, every line carrying a booking is reported
 * as not checked and the share is blocked: sharing into a hold nobody could confirm lists a hearing
 * with no session behind it. The results validator fails open; this check deliberately does not.
 */
@ApplicationScoped
public class SessionAvailabilityValidator {

    public static final String RULE_ID = "SESSION_AVAILABILITY";
    public static final String EXPIRED_MESSAGE = "The session you reserved has expired and been released. Select another session.";
    public static final String NOT_AVAILABLE_MESSAGE = "The session you have selected is now fully booked or is no longer available. Select another session.";
    public static final String NOT_CHECKED_MESSAGE = "The availability of the session you selected could not be checked. Try sharing again.";

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAvailabilityValidator.class);
    private static final String CJSCPPUID = "CJSCPPUID";
    private static final String BOOKING_REFERENCE = "bookingReference";
    private static final String STATUS_RESOURCE = "/unconfirmedBooking/status";
    private static final String QUERY_BOOKING_STATUS = "application/vnd.courtscheduler.query.booking.status+json";
    // The hold lapsed and could not be re-reserved; NOT_FOUND is also what a courtscheduler older than
    // EXPIRED answers for that.
    private static final Set<String> EXPIRED_STATUSES = Set.of("EXPIRED", "NOT_FOUND");

    @Inject
    @Value(key = "courtscheduler.base.url", defaultValue = "http://localhost:8080/listingcourtscheduler-api/rest/courtscheduler")
    protected String baseUri;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private HttpClient httpClient;

    @Inject
    private SystemUserProvider systemUserProvider;

    /**
     * @return the sessions that block the share, as validation issues and error messages; both empty
     *         when every session is held or no line carries a booking
     */
    public ValidationErrors validate(final List<SharedResultsCommandResultLineV2> resultLines, final Hearing hearing) {
        final Map<String, List<SharedResultsCommandResultLineV2>> linesByBooking = linesByBooking(resultLines);
        if (linesByBooking.isEmpty()) {
            return new ValidationErrors();
        }

        final Map<String, JsonNode> answers = askCourtscheduler(linesByBooking.keySet());
        final Map<String, String> blocked = new LinkedHashMap<>();
        linesByBooking.keySet().forEach(bookingId -> {
            final String message = messageFor(answers == null ? null : answers.get(bookingId));
            if (message != null) {
                blocked.put(bookingId, message);
            }
        });
        if (!blocked.isEmpty()) {
            LOGGER.info("Share blocked: sessions not available for bookings {}", blocked.keySet());
        }
        return toValidationErrors(linesByBooking, blocked, offenceTitles(hearing));
    }

    private static Map<String, List<SharedResultsCommandResultLineV2>> linesByBooking(final List<SharedResultsCommandResultLineV2> resultLines) {
        final Map<String, List<SharedResultsCommandResultLineV2>> linesByBooking = new LinkedHashMap<>();
        if (resultLines == null) {
            return linesByBooking;
        }
        resultLines.stream()
                .filter(line -> !line.getIsDeleted())
                .forEach(line -> {
                    final String bookingId = bookingReferenceOf(line);
                    if (bookingId != null) {
                        linesByBooking.computeIfAbsent(bookingId, id -> new ArrayList<>()).add(line);
                    }
                });
        return linesByBooking;
    }

    private static String bookingReferenceOf(final SharedResultsCommandResultLineV2 line) {
        if (line.getPrompts() == null) {
            return null;
        }
        return line.getPrompts().stream()
                .filter(prompt -> BOOKING_REFERENCE.equals(prompt.getPromptRef()))
                .map(SharedResultsCommandPrompt::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    /**
     * courtscheduler's answer per booking id, or null when it could not answer at all.
     */
    private Map<String, JsonNode> askCourtscheduler(final Set<String> bookingIds) {
        try {
            final HttpPost httpPost = new HttpPost(baseUri + STATUS_RESOURCE);
            httpPost.addHeader(CONTENT_TYPE, QUERY_BOOKING_STATUS);
            // courtscheduler grants this action to system users only, as it does for the hold itself.
            httpPost.addHeader(CJSCPPUID, systemUserProvider.getContextSystemUserId()
                    .orElseThrow(() -> new IllegalStateException("contextSystemUserId missing")).toString());
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(Map.of(
                    "bookingIds", new ArrayList<>(bookingIds),
                    "reserveIfExpired", true)), ContentType.APPLICATION_JSON));

            final HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
                LOGGER.error("courtscheduler booking status returned {} - blocking the share (fail-closed)",
                        httpResponse.getStatusLine().getStatusCode());
                // An unconsumed body keeps the pooled connection leased (see ResultsValidationClient).
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                return null;
            }
            try (final InputStream content = httpResponse.getEntity().getContent()) {
                final Map<String, JsonNode> answers = new HashMap<>();
                objectMapper.readTree(content).path("bookings")
                        .forEach(booking -> answers.put(booking.path("bookingId").asText(), booking));
                return answers;
            }
        } catch (final Exception ex) {
            LOGGER.error("courtscheduler booking status call failed - blocking the share (fail-closed)", ex);
            return null;
        }
    }

    /**
     * Null when the booking may be shared; otherwise what to tell the clerk. A booking courtscheduler
     * left out of its answer was not checked, so it blocks like an unanswered call.
     */
    private static String messageFor(final JsonNode answer) {
        if (answer == null) {
            return NOT_CHECKED_MESSAGE;
        }
        if (answer.path("safeToShare").asBoolean(false)) {
            return null;
        }
        return EXPIRED_STATUSES.contains(answer.path("status").asText()) ? EXPIRED_MESSAGE : NOT_AVAILABLE_MESSAGE;
    }

    /**
     * One issue for the offences, one for lines resulted against a defendant only, and an error
     * message for any other line - the same three places the results validator reports in.
     */
    private static ValidationErrors toValidationErrors(final Map<String, List<SharedResultsCommandResultLineV2>> linesByBooking,
                                                       final Map<String, String> blocked,
                                                       final Map<String, String> offenceTitles) {
        final Map<String, AffectedOffence> offences = new LinkedHashMap<>();
        final Map<String, AffectedDefendant> defendants = new LinkedHashMap<>();
        final Set<String> offenceCodes = new LinkedHashSet<>();
        final Set<String> defendantCodes = new LinkedHashSet<>();
        final Set<String> errorMessages = new LinkedHashSet<>();

        blocked.forEach((bookingId, message) -> linesByBooking.get(bookingId).forEach(line -> {
            if (line.getOffenceId() != null) {
                final String offenceId = line.getOffenceId().toString();
                offences.putIfAbsent(offenceId, new AffectedOffence()
                        .offenceId(offenceId)
                        .offenceTitle(offenceTitles.get(offenceId))
                        .message(message));
                addIfPresent(offenceCodes, line.getShortCode());
            } else if (line.getDefendantId() != null) {
                final String defendantId = line.getDefendantId().toString();
                defendants.putIfAbsent(defendantId, new AffectedDefendant().defendantId(defendantId).message(message));
                addIfPresent(defendantCodes, line.getShortCode());
            } else {
                errorMessages.add(message);
            }
        }));

        final List<ValidationIssue> issues = new ArrayList<>();
        if (!offences.isEmpty()) {
            issues.add(issue(ValidationIssue.ValidationLevelEnum.OFFENCE, offenceCodes)
                    .affectedOffences(new ArrayList<>(offences.values())));
        }
        if (!defendants.isEmpty()) {
            issues.add(issue(ValidationIssue.ValidationLevelEnum.DEFENDANT, defendantCodes)
                    .affectedDefendants(new ArrayList<>(defendants.values())));
        }
        return new ValidationErrors()
                .validationIssues(issues)
                .errorMessages(new ArrayList<>(errorMessages));
    }

    private static ValidationIssue issue(final ValidationIssue.ValidationLevelEnum level, final Set<String> resultCodes) {
        return new ValidationIssue()
                .ruleId(RULE_ID)
                .severity(ValidationIssue.SeverityEnum.ERROR)
                .validationLevel(level)
                .affectedResultCodes(new ArrayList<>(resultCodes))
                .affectedOffences(new ArrayList<>())
                .affectedDefendants(new ArrayList<>());
    }

    private static void addIfPresent(final Set<String> codes, final String code) {
        if (code != null) {
            codes.add(code);
        }
    }

    private static Map<String, String> offenceTitles(final Hearing hearing) {
        final Map<String, String> titles = new HashMap<>();
        if (hearing == null || hearing.getProsecutionCases() == null) {
            return titles;
        }
        hearing.getProsecutionCases().stream()
                .filter(prosecutionCase -> prosecutionCase.getDefendants() != null)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> defendant.getOffences() != null)
                .flatMap(defendant -> defendant.getOffences().stream())
                .filter(offence -> offence.getId() != null && offence.getOffenceTitle() != null)
                .forEach(offence -> titles.put(offence.getId().toString(), offence.getOffenceTitle()));
        return titles;
    }

    /** Whether the check found nothing to report. */
    public static boolean isEmpty(final ValidationErrors errors) {
        return (errors.getValidationIssues() == null || errors.getValidationIssues().isEmpty())
                && (errors.getErrorMessages() == null || errors.getErrorMessages().isEmpty());
    }
}
