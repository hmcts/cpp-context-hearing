package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class CompanyRepresentativeEventProcessor {

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("hearing.company-representative-change-ignored")
    public void publishPublicCompanyRepresentativeChangeIgnoredEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.company-representative-change-ignored").apply(event.payloadAsJsonObject()));
    }

}
