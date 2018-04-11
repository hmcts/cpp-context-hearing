package uk.gov.moj.cpp.hearing.event;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class NewModelVerdictUpdateEventProcessor {

    private final Enveloper enveloper;
    private final Sender sender;

    @Inject
    public NewModelVerdictUpdateEventProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }

    @Handles("hearing.offence-verdict-updated")
    public void offencePleaUpdate(final JsonEnvelope envelop) {


        this.sender.send(this.enveloper.withMetadataFrom(envelop, "public.hearing.verdict-updated")
                .apply(createObjectBuilder()
                        .add("hearingId", envelop.payloadAsJsonObject().getJsonString("hearingId"))
                        .build()
                )
        );
    }

}



