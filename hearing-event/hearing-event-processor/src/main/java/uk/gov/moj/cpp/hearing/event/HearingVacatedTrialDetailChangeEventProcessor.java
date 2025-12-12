package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class HearingVacatedTrialDetailChangeEventProcessor {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private Sender sender;


    @Handles("public.listing.vacated-trial-updated")
    public void handleListingVacatedTrialUpdate(final JsonEnvelope event) {

        final HearingVacatedTrialDetailsUpdateCommand hearingVacatedTrialDetailChanged = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingVacatedTrialDetailsUpdateCommand.class);
        final Metadata metadata = metadataFrom(event.metadata()).withName("hearing.update-vacated-trial-detail").build();

        if (hearingVacatedTrialDetailChanged.getAllocated()) {
            sender.send(envelopeFrom(metadata, event.payload()));
        }
    }
}
