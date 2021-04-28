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
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.RemoveOffencesFromExistingHearing;

import java.util.List;
import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateOffencesForDefendantCommandHandler extends AbstractCommandHandler {

    @Handles("hearing.command.update-offences-for-defendant")
    public void updateOffencesForDefendant(final JsonEnvelope envelope) throws EventStreamException {

        final UpdateOffencesForDefendantCommand command = convertToObject(envelope, UpdateOffencesForDefendantCommand.class);

        for (final DefendantCaseOffences addedOffence : command.getAddedOffences()) {
            for (final uk.gov.justice.core.courts.Offence offence : addedOffence.getOffences()) {
                aggregate(DefendantAggregate.class,
                        addedOffence.getDefendantId(),
                        envelope,
                        defendantAggregate -> defendantAggregate.lookupHearingsForNewOffenceOnDefendant(addedOffence.getDefendantId(), addedOffence.getProsecutionCaseId(), offence));
            }
        }

        for (final DefendantCaseOffences updateOffence : command.getUpdatedOffences()) {
            for (final uk.gov.justice.core.courts.Offence offence : updateOffence.getOffences()) {
                aggregate(OffenceAggregate.class,
                        offence.getId(),
                        envelope,
                        offenceAggregate -> offenceAggregate.lookupHearingsForEditOffenceOnOffence(updateOffence.getDefendantId(), offence));
            }
        }

        for (final DeletedOffences deletedOffence : command.getDeletedOffences()) {
            for (final UUID offenceId : deletedOffence.getOffences()) {
                aggregate(OffenceAggregate.class,
                        offenceId,
                        envelope,
                        offenceAggregate -> offenceAggregate.lookupHearingsForDeleteOffenceOnOffence(offenceId));
            }
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

    @Handles("hearing.command.update-offence-on-hearings")
    public void updateOffence(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForEditOffence foundHearingsForEditOffence = convertToObject(envelope, FoundHearingsForEditOffence.class);

        for (final UUID hearingId : foundHearingsForEditOffence.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.updateOffence(hearingId, foundHearingsForEditOffence.getDefendantId(), foundHearingsForEditOffence.getOffence()));
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


    @Handles("hearing.command.remove-offences-from-existing-hearing")
    public void removeOffencesFromExistingHearing(final JsonEnvelope envelope) throws EventStreamException {

        final RemoveOffencesFromExistingHearing removeOffencesFromExistingHearing = convertToObject(envelope, RemoveOffencesFromExistingHearing.class);

        final UUID hearingId = removeOffencesFromExistingHearing.getHearingId();
        final List<UUID> offenceIds = removeOffencesFromExistingHearing.getOffenceIds();

        aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.removeOffencesFromExistingHearing(hearingId, offenceIds));

    }
}