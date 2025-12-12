package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.offence.DefendantCaseOffences;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffences;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffenceV2;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffenceV2;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffenceV2;
import uk.gov.moj.cpp.hearing.domain.event.RemoveOffencesFromExistingHearing;

import java.util.List;
import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateOffencesForDefendantCommandHandler extends AbstractCommandHandler {

    @Handles("hearing.command.update-offences-for-defendant")
    public void updateOffencesForDefendant(final JsonEnvelope envelope) throws EventStreamException {

        final UpdateOffencesForDefendantCommand command = convertToObject(envelope, UpdateOffencesForDefendantCommand.class);

        for (final DefendantCaseOffences addedOffence : command.getAddedOffences()) {
            aggregate(DefendantAggregate.class,
                    addedOffence.getDefendantId(),
                    envelope,
                    defendantAggregate -> defendantAggregate.lookupHearingsForNewOffenceOnDefendantV2(addedOffence.getDefendantId(), addedOffence.getProsecutionCaseId(), addedOffence.getOffences()));
        }

        for (final DefendantCaseOffences updateOffence : command.getUpdatedOffences()) {
            aggregate(DefendantAggregate.class,
                    updateOffence.getDefendantId(),
                    envelope,
                    defendantAggregate -> defendantAggregate.lookupHearingsForEditOffenceOnOffence(updateOffence.getDefendantId(), updateOffence.getOffences()));
        }

        for (final DeletedOffences deletedOffence : command.getDeletedOffences()) {
            aggregate(DefendantAggregate.class,
                    deletedOffence.getDefendantId(),
                    envelope,
                    defendantAggregate -> defendantAggregate.lookupHearingsForDeleteOffenceOnOffence(deletedOffence.getOffences()));
        }
    }

    @Handles("hearing.command.add-new-offence-to-hearings")
    public void addOffenceForExistingHearing(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForNewOffence foundHearingsForNewOffence = convertToObject(envelope, FoundHearingsForNewOffence.class);

        for (final UUID hearingId : foundHearingsForNewOffence.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.addOffence(
                            hearingId,
                            foundHearingsForNewOffence.getDefendantId(),
                            foundHearingsForNewOffence.getProsecutionCaseId(),
                            foundHearingsForNewOffence.getOffence())
            );
        }
    }

    @Handles("hearing.command.add-new-offence-to-hearings-v2")
    public void addOffenceForExistingHearingV2(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForNewOffenceV2 foundHearingsForNewOffence = convertToObject(envelope, FoundHearingsForNewOffenceV2.class);

        for (final UUID hearingId : foundHearingsForNewOffence.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.addOffenceV2(
                            hearingId,
                            foundHearingsForNewOffence.getDefendantId(),
                            foundHearingsForNewOffence.getProsecutionCaseId(),
                            foundHearingsForNewOffence.getOffences())
            );
        }
    }

    @Handles("hearing.command.update-offence-on-hearings")
    public void updateOffence(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForEditOffence foundHearingsForEditOffence = convertToObject(envelope, FoundHearingsForEditOffence.class);

        for (final UUID hearingId : foundHearingsForEditOffence.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.updateOffence(hearingId, foundHearingsForEditOffence.getDefendantId(), foundHearingsForEditOffence.getOffence()));
        }
    }

    @Handles("hearing.command.update-offence-on-hearings-v2")
    public void updateOffenceV2(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForEditOffenceV2 foundHearingsForEditOffence = convertToObject(envelope, FoundHearingsForEditOffenceV2.class);

        for (final UUID hearingId : foundHearingsForEditOffence.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.updateOffenceV2(hearingId, foundHearingsForEditOffence.getDefendantId(), foundHearingsForEditOffence.getOffences()));
        }
    }

    @Handles("hearing.command.delete-offence-on-hearings")
    public void deleteOffence(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForDeleteOffence offenceWithHearingIds = convertToObject(envelope, FoundHearingsForDeleteOffence.class);

        for (UUID hearingId : offenceWithHearingIds.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.deleteOffence(offenceWithHearingIds.getId(), hearingId));
        }
    }

    @Handles("hearing.command.delete-offence-on-hearings-v2")
    public void deleteOffenceV2(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForDeleteOffenceV2 offenceWithHearingIds = convertToObject(envelope, FoundHearingsForDeleteOffenceV2.class);

        for (UUID hearingId : offenceWithHearingIds.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.deleteOffenceV2(offenceWithHearingIds.getIds(), hearingId));
        }
    }

    @Handles("hearing.command.remove-offences-from-existing-hearing")
    public void removeOffencesFromExistingHearing(final JsonEnvelope envelope) throws EventStreamException {

        final RemoveOffencesFromExistingHearing removeOffencesFromExistingHearing = convertToObject(envelope, RemoveOffencesFromExistingHearing.class);

        final UUID hearingId = removeOffencesFromExistingHearing.getHearingId();
        final List<UUID> offenceIds = removeOffencesFromExistingHearing.getOffenceIds();

        aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.removeOffencesFromExistingHearing(hearingId, offenceIds, "Hearing"));

    }

    @Handles("hearing.command.remove-offences-from-existing-allocated-hearing")
    public void removedOffencesFromAllocatedHearing(final JsonEnvelope envelope) throws EventStreamException {

        final RemoveOffencesFromExistingHearing removeOffencesFromExistingHearing = convertToObject(envelope, RemoveOffencesFromExistingHearing.class);

        final UUID hearingId = removeOffencesFromExistingHearing.getHearingId();
        final List<UUID> offenceIds = removeOffencesFromExistingHearing.getOffenceIds();

        aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.removeOffencesFromExistingHearing(hearingId, offenceIds, "Listing"));

    }
}