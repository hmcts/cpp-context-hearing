package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class NCESNotificationRequestedEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NCESNotificationRequestedEventProcessor.class);
    private Sender sender;
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public NCESNotificationRequestedEventProcessor(final Sender sender,
                                                   final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                                   final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    @Handles("hearing.event.nces-notification-requested")
    public void publishNcesNotificationRequestedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.nces-notification-requested event received {}", event.toObfuscatedDebugString());
        }

        final JsonObject jsonObject = event.payloadAsJsonObject().getJsonObject("financialOrderForDefendant");
        final FinancialOrderForDefendant  financialOrderForDefendant=this.jsonObjectToObjectConverter.convert(jsonObject, FinancialOrderForDefendant.class);

        final FinancialOrderForDefendant  publicFinancialOrderForDefendant= FinancialOrderForDefendant.newBuilder()
                .withCaseId(financialOrderForDefendant.getCaseId())
                .withDefendantId(financialOrderForDefendant.getDefendantId())
                .withDocumentContent(financialOrderForDefendant.getDocumentContent())
                .withEmailNotifications(financialOrderForDefendant.getEmailNotifications())
                .withHearingId(financialOrderForDefendant.getHearingId())
                .withMaterialId(financialOrderForDefendant.getMaterialId())
                .withAdditionalProperties(financialOrderForDefendant.getAdditionalProperties()).build();

        this.sender.send(envelop(objectToJsonObjectConverter.convert(publicFinancialOrderForDefendant)).withName("public.hearing.event.nces-notification-requested")
                                 .withMetadataFrom(event));
    }


}
