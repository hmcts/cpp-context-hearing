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

@ServiceComponent(COMMAND_HANDLER)
public class UnallocateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnallocateHearingCommandHandler.class);

    private static final String HEARING_COMMAND_UNALLOCATE_HEARING = "hearing.command.unallocate-hearing";
    private static final String HEARING_COMMAND_REMOVE_HEARING_FOR_PROSECUTION_CASES = "hearing.command.remove-hearing-for-prosecution-cases";
    private static final String HEARING_COMMAND_REMOVE_HEARING_FOR_DEFENDANTS = "hearing.command.remove-hearing-for-defendants";
    private static final String HEARING_COMMAND_REMOVE_HEARING_FOR_OFFENCES = "hearing.command.remove-hearing-for-offences";
    private static final String HEARING_ID = "hearingId";
    private static final String OFFENCE_IDS = "offenceIds";
    private static final String PROSECUTIONS_CASE_IDS = "prosecutionCaseIds";
    private static final String DEFENDANTS_IDS = "defendantIds";


    @Handles(HEARING_COMMAND_UNALLOCATE_HEARING)
    public void handleUnallocateHearing(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.unallocate-hearing' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> removedOffenceIds = extractIds(envelope, OFFENCE_IDS);
        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.unAllocateHearing(hearingId, removedOffenceIds));
    }

    @Handles(HEARING_COMMAND_REMOVE_HEARING_FOR_PROSECUTION_CASES)
    public void handleRemoveHearingForProsecutionCases(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.remove-hearing-for-cases' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> prosecutionCaseIds = extractIds(envelope, PROSECUTIONS_CASE_IDS);

        for (final UUID prosecutionCaseId : prosecutionCaseIds) {
            aggregate(CaseAggregate.class, prosecutionCaseId, envelope, a -> a.removeHearingForProsecutionCase(prosecutionCaseId, hearingId));
        }
    }

    @Handles(HEARING_COMMAND_REMOVE_HEARING_FOR_DEFENDANTS)
    public void handleRemoveHearingForDefendants(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.remove-hearing-for-defendants' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> defendantIds = extractIds(envelope, DEFENDANTS_IDS);

        for (final UUID defendantId : defendantIds) {
            aggregate(DefendantAggregate.class, defendantId, envelope, a -> a.removeHearingForDefendant(defendantId, hearingId));
        }
    }

    @Handles(HEARING_COMMAND_REMOVE_HEARING_FOR_OFFENCES)
    public void handleRemoveHearingForOffences(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.unallocate-hearing-for-offences' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> offenceIds = extractIds(envelope, OFFENCE_IDS);

        for (final UUID offenceId : offenceIds) {
            aggregate(OffenceAggregate.class, offenceId, envelope, a -> a.removeHearingForOffence(offenceId, hearingId));
        }
    }

    private List<UUID> extractIds(final JsonEnvelope envelope, final String field) {
        final JsonArray jsonArray = envelope.payloadAsJsonObject().getJsonArray((field));
        final ArrayList<JsonString> jsonObjects = new ArrayList<>(jsonArray.getValuesAs(JsonString.class));
        return jsonObjects.stream().map(JsonString::getString).map(UUID::fromString).collect(Collectors.toList());
    }
}
