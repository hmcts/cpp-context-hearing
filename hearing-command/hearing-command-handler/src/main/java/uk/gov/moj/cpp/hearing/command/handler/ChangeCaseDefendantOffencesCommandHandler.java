package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.offence.BaseDefendantOffence;
import uk.gov.moj.cpp.hearing.command.offence.CaseDefendantOffencesChangedCommand;
import uk.gov.moj.cpp.hearing.command.offence.DefendantOffence;
import uk.gov.moj.cpp.hearing.command.offence.DefendantOffences;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffences;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;

import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class ChangeCaseDefendantOffencesCommandHandler extends AbstractCommandHandler {

    @Handles("hearing.command.defendant-offences-changed")
    public void updateCaseDefendantOffences(final JsonEnvelope envelope) throws EventStreamException {

        final CaseDefendantOffencesChangedCommand command = convertToObject(envelope, CaseDefendantOffencesChangedCommand.class);

        for (DefendantOffences addedOffence : command.getAddedOffences()) {
            for (DefendantOffence offence : addedOffence.getOffences()) {
                aggregate(DefendantAggregate.class,
                        addedOffence.getDefendantId(),
                        envelope,
                        defendantAggregate -> defendantAggregate.lookupHearingsForNewOffenceOnDefendant(addedOffence.getDefendantId(), addedOffence.getCaseId(),offence));
            }
        }

        for (DefendantOffences updateOffence : command.getUpdatedOffences()) {
            for (DefendantOffence offence : updateOffence.getOffences()) {
                aggregate(OffenceAggregate.class,
                        offence.getId(),
                        envelope,
                        offenceAggregate -> offenceAggregate.lookupHearingsForEditOffenceOnOffence(offence));
            }
        }

        for (DeletedOffences deletedOffence : command.getDeletedOffences()) {
            for (UUID offenceId : deletedOffence.getOffences()) {
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

        for (UUID hearingId : foundHearingsForNewOffence.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.addOffence(
                            hearingId,
                            foundHearingsForNewOffence.getDefendantId(),
                            foundHearingsForNewOffence.getCaseId(),
                            BaseDefendantOffence.builder()
                                    .withId(foundHearingsForNewOffence.getId())
                                    .withOffenceCode(foundHearingsForNewOffence.getOffenceCode())
                                    .withWording(foundHearingsForNewOffence.getWording())
                                    .withStartDate(foundHearingsForNewOffence.getStartDate())
                                    .withEndDate(foundHearingsForNewOffence.getEndDate())
                                    .withCount(foundHearingsForNewOffence.getCount())
                                    .withConvictionDate(foundHearingsForNewOffence.getConvictionDate())
                                    .build()
                    ));
        }
    }

    @Handles("hearing.command.update-offence-on-hearings")
    public void updateOffence(final JsonEnvelope envelope) throws EventStreamException {

        final FoundHearingsForEditOffence foundHearingsForEditOffence = convertToObject(envelope, FoundHearingsForEditOffence.class);

        for (UUID hearingId : foundHearingsForEditOffence.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.updateOffence(hearingId, BaseDefendantOffence.builder()
                            .withId(foundHearingsForEditOffence.getId())
                            .withOffenceCode(foundHearingsForEditOffence.getOffenceCode())
                            .withWording(foundHearingsForEditOffence.getWording())
                            .withStartDate(foundHearingsForEditOffence.getStartDate())
                            .withEndDate(foundHearingsForEditOffence.getEndDate())
                            .withCount(foundHearingsForEditOffence.getCount())
                            .withConvictionDate(foundHearingsForEditOffence.getConvictionDate())
                            .build()));
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
}