package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.hearing.courts.UpdateCaseMarkers;
import uk.gov.justice.hearing.courts.UpdateCaseMarkersWithHearings;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class CaseMarkersCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CaseMarkersCommandHandler.class.getName());

    @Handles("hearing.command.update-case-markers")
    public void updateCaseMarkers(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-case-markers event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateCaseMarkers updateCaseMarkers = convertToObject(envelope, UpdateCaseMarkers.class);

        final Stream<Object> objectStream = aggregate(CaseAggregate.class,updateCaseMarkers.getProsecutionCaseId()).enrichUpdateCaseMarkersWithHearingIds(updateCaseMarkers.getProsecutionCaseId(), updateCaseMarkers.getCaseMarkers());
        if(objectStream != null) {
            aggregate(CaseAggregate.class,
                    updateCaseMarkers.getProsecutionCaseId(),
                    envelope,
                    caseAggregate -> objectStream);
        }
    }

    @Handles("hearing.command.update-case-markers-with-associated-hearings")
    public void updateCaseMarkersForAssociatedHearings(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.enrich-update-case-markers-with-associated-hearings event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateCaseMarkersWithHearings updateCaseMarkersWithHearings = convertToObject(envelope, UpdateCaseMarkersWithHearings.class);
        for (final UUID hearingId : updateCaseMarkersWithHearings.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope,
                    hearingAggregate -> hearingAggregate.updateCaseMarkers(hearingId, updateCaseMarkersWithHearings.getProsecutionCaseId(), updateCaseMarkersWithHearings.getCaseMarkers()));

        }
    }
}
