package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command Handler to manage commands related to duplicate hearings. These are hearings that have
 * erroneously been created on the system as duplicates of other hearings and need to be managed
 * (removed).
 */
@ServiceComponent(COMMAND_HANDLER)
public class DuplicateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DuplicateHearingCommandHandler.class.getName());

    private static final String HEARING_ID_FIELD = "hearingId";
    private static final String OVERWRITE_RESULTS_FIELD = "overwriteWithResults";

    /**
     * Initial command to mark a hearing as a duplicate (called from command-api).
     *
     * @param envelope - command from command-api
     * @throws EventStreamException
     */
    @Handles("hearing.command.mark-as-duplicate")
    public void markAsDuplicateHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.mark-as-duplicate {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID_FIELD));
        final boolean overwriteWithResults = envelope.payloadAsJsonObject().getBoolean(OVERWRITE_RESULTS_FIELD, false);

        LOGGER.info("Marking hearing with id {} as duplicate, overwriting results is: {}", hearingId, overwriteWithResults);

        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.markAsDuplicate(hearingId, overwriteWithResults));
    }

    /**
     * Subsequent commands called off the back of the initial mark-as-duplicate command above, that
     * is used to tidy-up cases.
     *
     * @param envelope - command from event-processor
     * @throws EventStreamException
     */
    @Handles("hearing.command.mark-as-duplicate-for-cases")
    public void markAsDuplicateHearingForCases(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.mark-as-duplicate-for-cases {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID_FIELD));
        final List<UUID> caseIds = extractIds(envelope, "prosecutionCaseIds");

        for (final UUID caseId : caseIds) {
            aggregate(CaseAggregate.class, caseId, envelope, a -> a.markHearingAsDuplicate(caseId, hearingId));
        }
    }

    /**
     * Subsequent commands called off the back of the initial mark-as-duplicate command above, that
     * is used to tidy-up defendants.
     *
     * @param envelope - command from event-processor
     * @throws EventStreamException
     */
    @Handles("hearing.command.mark-as-duplicate-for-defendants")
    public void markAsDuplicateHearingForDefendants(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.mark-as-duplicate-for-defendants {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID_FIELD));
        final List<UUID> defendantIds = extractIds(envelope, "defendantIds");

        for (final UUID defendantId : defendantIds) {
            aggregate(DefendantAggregate.class, defendantId, envelope, a -> a.markHearingAsDuplicate(defendantId, hearingId));
        }
    }

    /**
     * Subsequent commands called off the back of the initial mark-as-duplicate command above, that
     * is used to tidy-up offences.
     *
     * @param envelope - command from event-processor
     * @throws EventStreamException
     */
    @Handles("hearing.command.mark-as-duplicate-for-offences")
    public void markAsDuplicateHearingForOffences(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.mark-as-duplicate-for-offences {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID_FIELD));
        final List<UUID> offenceIds = extractIds(envelope, "offenceIds");

        for (final UUID offenceId : offenceIds) {
            aggregate(OffenceAggregate.class, offenceId, envelope, a -> a.markHearingAsDuplicate(offenceId, hearingId));
        }
    }

    /**
     * Converts json array of a given field into a list of UUID's.
     *
     * @param envelope - the envelope containing the payload to extract the ids from.
     * @param field    - the array field containing the ids to extract.
     * @return a collection of UUIDs taken from the provided array field.
     */
    private List<UUID> extractIds(final JsonEnvelope envelope, final String field) {
        final JsonArray jsonArray = envelope.payloadAsJsonObject().getJsonArray((field));
        final ArrayList<JsonString> jsonObjects = new ArrayList<>(jsonArray.getValuesAs(JsonString.class));
        return jsonObjects.stream().map(JsonString::getString).map(UUID::fromString).collect(Collectors.toList());
    }
}
