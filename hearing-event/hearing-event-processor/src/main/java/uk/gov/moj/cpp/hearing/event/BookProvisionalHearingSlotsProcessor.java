package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.listing.common.azure.ProvisionalBookingService;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class BookProvisionalHearingSlotsProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookProvisionalHearingSlotsProcessor.class);
    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;
    private final ProvisionalBookingService provisionalBookingService;

    @Inject
    public BookProvisionalHearingSlotsProcessor(final Sender sender,
                                                final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                                final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                                final ProvisionalBookingService provisionalBookingService) {
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.provisionalBookingService = provisionalBookingService;
    }

    @Handles("hearing.event.book-provisional-hearing-slots")
    public void handleBookProvisionalHearingSlots(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.book-provisional-hearing-slots event received {}", event.toObfuscatedDebugString());
        }

        final BookProvisionalHearingSlots bookProvisionalHearingSlots = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), BookProvisionalHearingSlots.class);
        final JsonArrayBuilder arrayBuilder = createArrayBuilder();
        bookProvisionalHearingSlots.getSlots().forEach(
                csi -> arrayBuilder.add(
                        createObjectBuilder().add("courtScheduleId", csi.toString()).build()
                )
        );

        final Response response = provisionalBookingService.bookSlots(arrayBuilder.build());

        final JsonObject responseJson = objectToJsonObjectConverter.convert(response.getEntity());
        //raise public event for UI
        if (response.getStatus() == SC_OK) {
            sender.send(Enveloper.envelop(Json.createObjectBuilder().add("bookingId", responseJson.getString("bookingId")).build())
                    .withName("public.hearing.hearing-slots-provisionally-booked")
                    .withMetadataFrom(event));
        } else {
            sender.send(Enveloper.envelop(Json.createObjectBuilder().add("error", responseJson.getString("errorMessage")).build())
                    .withName("public.hearing.hearing-slots-provisionally-booked")
                    .withMetadataFrom(event));
        }

    }
}
