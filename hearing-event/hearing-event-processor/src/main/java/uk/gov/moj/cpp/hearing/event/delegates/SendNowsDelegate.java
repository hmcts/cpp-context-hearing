package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;

import javax.inject.Inject;

public class SendNowsDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public SendNowsDelegate(final Enveloper enveloper,
                            final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    public void send(final Sender sender, final JsonEnvelope event, CreateNowsRequest nowsRequest) {
        final GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand();
        generateNowsCommand.setCreateNowsRequest(nowsRequest);
        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.generate-nows").apply(this.objectToJsonObjectConverter.convert(generateNowsCommand)));
    }
}
