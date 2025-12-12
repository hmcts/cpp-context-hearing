package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
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
public class DeleteHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteHearingCommandHandler.class);

    private static final String HEARING_COMMAND_DELETE_HEARING = "hearing.command.delete-hearing";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_PROSECUTION_CASES = "hearing.command.delete-hearing-for-prosecution-cases";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_DEFENDANTS = "hearing.command.delete-hearing-for-defendants";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_OFFENCES = "hearing.command.delete-hearing-for-offences";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_COURT_APPLICATIONS = "hearing.command.delete-hearing-for-court-applications";
    private static final String HEARING_ID = "hearingId";

    private static final String HEARING_COMMAND_DELETE_HEARING_BDF = "hearing.command.delete-hearing-bdf";

    @Handles(HEARING_COMMAND_DELETE_HEARING)
    public void handleDeleteHearing(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.delete-hearing' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.deleteHearing(hearingId));
    }

    @Handles(HEARING_COMMAND_DELETE_HEARING_BDF)
    public void handleDeleteHearingBdf(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.delete-hearing-bdf' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.deleteHearingBdf(hearingId));
    }

    @Handles(HEARING_COMMAND_DELETE_HEARING_FOR_PROSECUTION_CASES)
    public void handleDeleteHearingForCases(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.delete-hearing-for-cases' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> prosecutionCaseIds = extractIds(envelope, "prosecutionCaseIds");

        for (final UUID prosecutionCaseId : prosecutionCaseIds) {
            aggregate(CaseAggregate.class, prosecutionCaseId, envelope, a -> a.deleteHearingForProsecutionCase(prosecutionCaseId, hearingId));
        }
    }

    @Handles(HEARING_COMMAND_DELETE_HEARING_FOR_DEFENDANTS)
    public void handleDeleteHearingForDefendants(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.delete-hearing-for-defendants' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> defendantIds = extractIds(envelope, "defendantIds");

        for (final UUID defendantId : defendantIds) {
            aggregate(DefendantAggregate.class, defendantId, envelope, a -> a.deleteHearingForDefendant(defendantId, hearingId));
        }
    }

    @Handles(HEARING_COMMAND_DELETE_HEARING_FOR_OFFENCES)
    public void handleDeleteHearingForOffences(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.delete-hearing-for-offences' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> offenceIds = extractIds(envelope, "offenceIds");

        for (final UUID offenceId : offenceIds) {
            aggregate(OffenceAggregate.class, offenceId, envelope, a -> a.deleteHearingForOffence(offenceId, hearingId));
        }
    }

    @Handles(HEARING_COMMAND_DELETE_HEARING_FOR_COURT_APPLICATIONS)
    public void handleDeleteHearingForCourtApplications(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.delete-hearing-for-court-applications' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final List<UUID> courtApplicationIds = extractIds(envelope, "courtApplicationIds");

        for (final UUID courtApplicationId : courtApplicationIds) {
            aggregate(ApplicationAggregate.class, courtApplicationId, envelope, a -> a.deleteHearingForCourtApplication(courtApplicationId, hearingId));
        }

    }

    private List<UUID> extractIds(final JsonEnvelope envelope, final String field) {
        final JsonArray jsonArray = envelope.payloadAsJsonObject().getJsonArray((field));
        final ArrayList<JsonString> jsonObjects = new ArrayList<>(jsonArray.getValuesAs(JsonString.class));
        return jsonObjects.stream().map(JsonString::getString).map(UUID::fromString).collect(Collectors.toList());
    }
}
