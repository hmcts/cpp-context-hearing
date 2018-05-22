package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.offence.AddedOffence;
import uk.gov.moj.cpp.hearing.command.offence.CaseDefendantOffencesChangedCommand;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffence;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantOffenceWithHearingIds;
import uk.gov.moj.cpp.hearing.domain.event.DeleteOffenceFromHearings;
import uk.gov.moj.cpp.hearing.domain.event.UpdateOffenceOnHearings;

import javax.inject.Inject;
import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class ChangeCaseDefendantOffencesCommandHandler extends AbstractCommandHandler {

    @Inject
    public ChangeCaseDefendantOffencesCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                                     final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.update-case-defendant-offences")
    public void updateCaseDefendantOffences(final JsonEnvelope envelope) throws EventStreamException {

        final CaseDefendantOffencesChangedCommand command = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CaseDefendantOffencesChangedCommand.class);

        for (AddedOffence addedOffence : command.getAddedOffences()) {
            for (UpdatedOffence offence : addedOffence.getOffences()) {
                aggregate(DefendantAggregate.class,
                        addedOffence.getDefendantId(),
                        envelope,
                        defendantAggregate -> defendantAggregate.enrichNewOffenceWithAllHearingIdsAssociatedToDefendant(addedOffence.getDefendantId(), addedOffence.getCaseId(), offence));
            }
        }

        for (UpdatedOffence offence : command.getUpdatedOffences()) {
            aggregate(OffenceAggregate.class,
                    offence.getId(),
                    envelope,
                    offenceAggregate -> offenceAggregate.enrichEditOffenceCommandWithHearingIds(offence));
        }

        for (DeletedOffence offence : command.getDeletedOffences()) {
            aggregate(OffenceAggregate.class,
                    offence.getId(),
                    envelope,
                    offenceAggregate -> offenceAggregate.enrichDeleteOffenceCommandWithHearingIds(offence.getId()));
        }
    }

    @Handles("hearing.add-case-defendant-offence")
    public void addOffenceForExistingHearing(final JsonEnvelope envelope) throws EventStreamException {

        final CaseDefendantOffenceWithHearingIds caseDefendantOffenceWithHearingIds = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CaseDefendantOffenceWithHearingIds.class);

        for (UUID hearingId : caseDefendantOffenceWithHearingIds.getHearingIds()) {
            aggregate(NewModelHearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.addOffence(
                            hearingId,
                            caseDefendantOffenceWithHearingIds.getDefendantId(),
                            caseDefendantOffenceWithHearingIds.getCaseId(),
                            UpdatedOffence.builder()
                                    .withId(caseDefendantOffenceWithHearingIds.getId())
                                    .withOffenceCode(caseDefendantOffenceWithHearingIds.getOffenceCode())
                                    .withWording(caseDefendantOffenceWithHearingIds.getWording())
                                    .withStartDate(caseDefendantOffenceWithHearingIds.getStartDate())
                                    .withEndDate(caseDefendantOffenceWithHearingIds.getEndDate())
                                    .withCount(caseDefendantOffenceWithHearingIds.getCount())
                                    .withConvictionDate(caseDefendantOffenceWithHearingIds.getConvictionDate())
                                    .build()
                    ));
        }
    }

    @Handles("hearing.update-case-defendant-offence")
    public void updateOffence(final JsonEnvelope envelope) throws EventStreamException {

        final UpdateOffenceOnHearings updateOffenceOnHearings = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), UpdateOffenceOnHearings.class);

        for (UUID hearingId : updateOffenceOnHearings.getHearingIds()) {
            aggregate(NewModelHearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.updateOffence(hearingId, UpdatedOffence.builder()
                            .withId(updateOffenceOnHearings.getId())
                            .withOffenceCode(updateOffenceOnHearings.getOffenceCode())
                            .withWording(updateOffenceOnHearings.getWording())
                            .withStartDate(updateOffenceOnHearings.getStartDate())
                            .withEndDate(updateOffenceOnHearings.getEndDate())
                            .withCount(updateOffenceOnHearings.getCount())
                            .withConvictionDate(updateOffenceOnHearings.getConvictionDate())
                            .build()));
        }
    }

    @Handles("hearing.delete-case-defendant-offence")
    public void deleteOffence(final JsonEnvelope envelope) throws EventStreamException {

        final DeleteOffenceFromHearings offenceWithHearingIds = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), DeleteOffenceFromHearings.class);

        for (UUID hearingId : offenceWithHearingIds.getHearingIds()) {
            aggregate(NewModelHearingAggregate.class, hearingId, envelope, hearingAggregate ->
                    hearingAggregate.deleteOffence(offenceWithHearingIds.getId(), hearingId));
        }
    }
}