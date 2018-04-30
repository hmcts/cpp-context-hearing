package uk.gov.moj.cpp.hearing.event;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseDefendantDetailsChangeEventProcessor {

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("public.progression.case-defendant-changed")
    public void processPublicCaseDefendantChanged(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.update-case-defendant-details").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.update-case-defendant-details-enriched-with-hearing-ids")
    public void enrichDefendantDetails(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.update-case-defendant-details-against-hearing-aggregate").apply(event.payloadAsJsonObject()));
    }
}