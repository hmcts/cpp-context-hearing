package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.eventlog.Case;
import uk.gov.moj.cpp.hearing.eventlog.CourtCentre;
import uk.gov.moj.cpp.hearing.eventlog.Hearing;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;
import uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class LogEventHearingEventProcessor {

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEventHearingEventProcessor.class);

    @Handles("hearing.hearing-event-logged")
    public void publishHearingEventLoggedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-event-logged event received {}", event.toObfuscatedDebugString());
        }

        final HearingEventLogged hearingEventLogged = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingEventLogged.class);

        final PublicHearingEventLogged publicHearingEventLogged = PublicHearingEventLogged.builder()
                .withHearingEventDefinition(HearingEventDefinition.builder()
                        .withHearingEventDefinitionId(hearingEventLogged.getHearingEventDefinitionId())
                        .withPriority(!hearingEventLogged.isAlterable())
                )
                .withHearingEvent(HearingEvent.builder()
                        .withHearingEventId(hearingEventLogged.getHearingEventId())
                        .withLastHearingEventId(hearingEventLogged.getLastHearingEventId())
                        .withEventTime(hearingEventLogged.getEventTime())
                        .withLastModifiedTime(hearingEventLogged.getLastModifiedTime())
                        .withRecordedLabel(hearingEventLogged.getRecordedLabel())
                )
                .withCase(Case.builder()
                        .withCaseUrn(hearingEventLogged.getCaseURN())
                )
                .withHearing(Hearing.builder()
                        .withHearingType(hearingEventLogged.getHearingType().getDescription())
                        .withCourtCentre(CourtCentre.builder()
                                .withCourtCentreId(hearingEventLogged.getCourtCentre().getId())
                                .withCourtCentreName(hearingEventLogged.getCourtCentre().getName())
                                .withCourtRoomId(hearingEventLogged.getCourtCentre().getRoomId())
                                .withCourtRoomName(hearingEventLogged.getCourtCentre().getRoomName())
                        )
                )
                .build();

        this.sender.send(this.enveloper.withMetadataFrom(event, hearingEventLogged.getLastHearingEventId() != null ? "public.hearing.event-timestamp-corrected" : "public.hearing.event-logged")
                .apply(this.objectToJsonValueConverter.convert(publicHearingEventLogged)));
    }

    @Handles("hearing.hearing-events-updated")
    public void publishHearingEventsUpdatedEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.events-updated")
                        .apply(event.payloadAsJsonObject()));
    }
    @Handles("hearing.hearing-event-ignored")
    public void publishHearingEventIgnoredPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-event-ignored event received {}", event.toObfuscatedDebugString());
        }

        final HearingEventIgnored hearingEventIgnored = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingEventIgnored.class);

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.event-ignored")
                .apply(this.objectToJsonValueConverter.convert(hearingEventIgnored)));
    }
}
