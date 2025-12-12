package uk.gov.moj.cpp.hearing.event;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;
import uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class BookProvisionalHearingSlotsProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookProvisionalHearingSlotsProcessor.class);
    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ProvisionalBookingService provisionalBookingService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Inject
    public BookProvisionalHearingSlotsProcessor(final Sender sender,
                                                final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                                final ProvisionalBookingService provisionalBookingService) {
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
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
                bookProvisionalHearingSlotsCommand -> {
                    final String hearingStartTimeStr = Objects.nonNull(bookProvisionalHearingSlotsCommand.getHearingStartTime()) ?
                            bookProvisionalHearingSlotsCommand.getHearingStartTime().format(DATE_TIME_FORMATTER) : StringUtils.EMPTY;
                    arrayBuilder.add(
                            createObjectBuilder().add("courtScheduleId", bookProvisionalHearingSlotsCommand.getCourtScheduleId().toString())
                                    .add("hearingStartTime", hearingStartTimeStr)
                                    .build()
                    );
                }

        );

        final JsonObject payload = createObjectBuilder().add("provisionalSlots",arrayBuilder.build()).build();

        final ProvisionalBookingServiceResponse provisionalBookingServiceResponse = provisionalBookingService.bookSlots(payload);


        //raise public event for UI
        if (!provisionalBookingServiceResponse.hasError()) {
            sender.send(Enveloper.envelop(Json.createObjectBuilder().add("bookingId", provisionalBookingServiceResponse.getBookingId()).build())
                    .withName("public.hearing.hearing-slots-provisionally-booked")
                    .withMetadataFrom(event));
        } else {
            sender.send(Enveloper.envelop(Json.createObjectBuilder().add("error", provisionalBookingServiceResponse.getErrorMessage()).build())
                    .withName("public.hearing.hearing-slots-provisionally-booked")
                    .withMetadataFrom(event));
        }
    }
}
