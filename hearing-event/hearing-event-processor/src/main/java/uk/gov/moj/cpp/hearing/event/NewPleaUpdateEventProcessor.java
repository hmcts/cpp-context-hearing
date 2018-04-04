package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import javax.inject.Inject;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(EVENT_PROCESSOR)
public class NewPleaUpdateEventProcessor {

    private final Enveloper enveloper;
    private final Sender sender;
    private final Requester requester;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonValueConverter objectToJsonValueConverter;
    
    @Inject
    public NewPleaUpdateEventProcessor(final Enveloper enveloper, final Sender sender, final Requester requester,
            final JsonObjectToObjectConverter jsonObjectToObjectConverter,
            final ObjectToJsonValueConverter objectToJsonValueConverter) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.requester = requester;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonValueConverter = objectToJsonValueConverter;
    }
    
    @Handles("hearing.offence-plea-updated")
    public void offencePleaUpdate(final JsonEnvelope command) {
        System.out.println(">>>>>>>> SHOULD SEND TO UPDATE OFFENCE: " + command);
    }
}