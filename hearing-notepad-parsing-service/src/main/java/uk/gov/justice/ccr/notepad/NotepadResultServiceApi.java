package uk.gov.justice.ccr.notepad;


import uk.gov.justice.ccr.notepad.stub.DummyResultDefinitionGenerator;
import uk.gov.justice.ccr.notepad.stub.DummyResultPromptGenerator;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(Component.QUERY_API)
public class NotepadResultServiceApi {

    @Inject
    Enveloper enveloper;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;


    @Handles("hearing.notepad.parse-result-definition")
    public JsonEnvelope getResultDefinition(final JsonEnvelope envelope) {
        String originalText = envelope.payloadAsJsonObject().getString("originalText");
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-definition-response")
                .apply(objectToJsonObjectConverter.convert(DummyResultDefinitionGenerator.getResultDefinition(originalText)));
    }

    @Handles("hearing.notepad.parse-result-prompt")
    public JsonEnvelope getResultPrompt(final JsonEnvelope envelope) {
        String resultCode = envelope.payloadAsJsonObject().getString("resultCode");
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-definition-response")
                .apply(objectToJsonObjectConverter.convert(DummyResultPromptGenerator.getPrompt(resultCode)));
    }

}
