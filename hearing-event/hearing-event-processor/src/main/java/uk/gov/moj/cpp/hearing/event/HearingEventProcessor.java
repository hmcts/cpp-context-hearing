package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import javax.inject.Inject;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    private final Enveloper enveloper;
    private final Sender sender;

    @Inject
    public HearingEventProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("hearing.results-shared")
    public void publishHearingResultsSharedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.resulted").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.result-amended")
    public void publishHearingResultAmendedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.result-amended").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.draft-result-saved")
    public void publicDraftResultSavedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.draft-result-saved").apply(event.payloadAsJsonObject()));
    }

}
