package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesQueried;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.UUID;
import java.util.stream.Collectors;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

@ServiceComponent(EVENT_PROCESSOR)
public class ComputeOutstandingFinesRequestedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeOutstandingFinesRequestedProcessor.class);

    @Inject
    private Sender sender;

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Handles("hearing.compute-outstanding-fines-requested")
    public void publicComputeOutstandingFinesRequested(final JsonEnvelope event) {
        final OutstandingFinesQueried finesQueried = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(),
                OutstandingFinesQueried.class);


        final Envelope<JsonObject> envelope = envelop(createObjectBuilder()
                .add("courtCentreId", finesQueried.getCourtCentreId().toString())
                .add("courtRoomIds", finesQueried.getCourtRoomIds().stream().map(UUID::toString).collect(Collectors.joining(",")))
                .add("hearingDate", finesQueried.getHearingDate().toString())
                .build()
        ).withName("hearing.defendant.info").withMetadataFrom(event);

        final JsonEnvelope courtBasedDefendantQueryInformation = requester.request(envelopeFrom(envelope.metadata(), envelope.payload()));
        final JsonObject payload = courtBasedDefendantQueryInformation.payloadAsJsonObject();
        if (!payload.isEmpty()) {
            this.sender.send(envelopeFrom(
                    metadataFrom(event.metadata()).withName("stagingenforcement.court.rooms.outstanding-fines"),
                    payload));
        }else {
            LOGGER.info("hearing.defendant.info response Information is empty");
        }

    }

}
