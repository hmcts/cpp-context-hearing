package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.hearing.details.RecordNextHearingDayUpdatedCommand;
import javax.inject.Inject;

@SuppressWarnings("squid:S3655")
@ServiceComponent(EVENT_PROCESSOR)
public class NextHearingEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NextHearingEventProcessor.class);

    private static final String PUBLIC_EVENTS_LISTING_NEXT_HEARING_DAY_CHANGED = "public.events.listing.next-hearing-day-changed";

    private static final String HEARING_COMMAND_RECORD_NEXT_HEARING_DAY_UPDATED = "hearing.command.record-next-hearing-day-updated";

    private static final String EVENT_RECEIVED_LOG_TEMPLATE = "{} event received {}";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Handles(PUBLIC_EVENTS_LISTING_NEXT_HEARING_DAY_CHANGED)
    public void processNextHearingDayChangedPublicEvent(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_LOG_TEMPLATE, PUBLIC_EVENTS_LISTING_NEXT_HEARING_DAY_CHANGED, event.toObfuscatedDebugString());
        }

        final PublicListingNextHearingDayChanged publicListingNextHearingDayChanged = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), PublicListingNextHearingDayChanged.class);

        final RecordNextHearingDayUpdatedCommand command = new RecordNextHearingDayUpdatedCommand(
                publicListingNextHearingDayChanged.getHearingId(),
                publicListingNextHearingDayChanged.getSeedingHearingId(),
                publicListingNextHearingDayChanged.geHearingStartDate());

        sender.send(envelop(objectToJsonObjectConverter.convert(command))
                .withName(HEARING_COMMAND_RECORD_NEXT_HEARING_DAY_UPDATED)
                .withMetadataFrom(event));

    }



}
