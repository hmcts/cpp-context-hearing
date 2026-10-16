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

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class BookProvisionalHearingSlotsProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookProvisionalHearingSlotsProcessor.class);
    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ProvisionalBookingService provisionalBookingService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Success and failure are two events, not one event with two payload shapes.
     *
     * <p>Both used to be published under the booked name, discriminated only by whether the
     * payload carried a bookingId or an error. That forced every consumer to re-inspect the
     * payload to learn whether the command had worked - cpp-ui-hearing's commandSync resolves on
     * the event NAME, so a refusal arrived as an ordinary success value and had to be converted
     * back into an error by hand. It also made the published schema a {@code oneOf}, which meant
     * neither branch could be made required and a payload carrying BOTH fields validated against
     * neither.
     *
     * <p>Splitting the names lets a consumer register the failure name as commandSync's
     * {@code errorEvent} and get an errored observable for free, and lets each schema state
     * exactly what its payload must contain.
     */
    private static final String HEARING_SLOTS_PROVISIONALLY_BOOKED = "public.hearing.hearing-slots-provisionally-booked";
    private static final String HEARING_SLOTS_UNCONFIRMED_BOOKING_FAILED = "public.hearing.hearing-slots-unconfirmed-booking-failed";

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
            LOGGER.debug("hearing.book-unconfirmed-hearing-slots event received {}", event.toObfuscatedDebugString());
        }

        final BookProvisionalHearingSlots bookProvisionalHearingSlots = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), BookProvisionalHearingSlots.class);
        final JsonArrayBuilder arrayBuilder = createArrayBuilder();
        bookProvisionalHearingSlots.getSlots().forEach(
                bookProvisionalHearingSlotsCommand -> {
                    final String hearingStartTimeStr = Objects.nonNull(bookProvisionalHearingSlotsCommand.getHearingStartTime()) ?
                            bookProvisionalHearingSlotsCommand.getHearingStartTime().format(DATE_TIME_FORMATTER) : StringUtils.EMPTY;
                    final JsonObjectBuilder slotBuilder = createObjectBuilder()
                            .add("courtScheduleId", bookProvisionalHearingSlotsCommand.getCourtScheduleId().toString())
                            .add("hearingStartTime", hearingStartTimeStr);
                    // Omit rather than send null: JsonObjectBuilder.add rejects nulls, and
                    // courtscheduler treats an absent duration as "slot-based, none needed".
                    if (Objects.nonNull(bookProvisionalHearingSlotsCommand.getDuration())) {
                        slotBuilder.add("duration", bookProvisionalHearingSlotsCommand.getDuration());
                    }
                    arrayBuilder.add(slotBuilder.build());
                }

        );

        final JsonObjectBuilder payloadBuilder = createObjectBuilder().add("provisionalSlots", arrayBuilder.build());
        // Omit rather than send null: JsonObjectBuilder.add rejects nulls, and courtscheduler
        // treats an absent bookingId as "first pick, mint one".
        if (StringUtils.isNotBlank(bookProvisionalHearingSlots.getBookingId())) {
            payloadBuilder.add("bookingId", bookProvisionalHearingSlots.getBookingId());
        }
        final JsonObject payload = payloadBuilder.build();

        final ProvisionalBookingServiceResponse provisionalBookingServiceResponse = provisionalBookingService.bookSlots(payload);


        //raise public event for UI
        if (!provisionalBookingServiceResponse.hasError()) {
            sender.send(Enveloper.envelop(createObjectBuilder().add("bookingId", provisionalBookingServiceResponse.getBookingId()).build())
                    .withName(HEARING_SLOTS_PROVISIONALLY_BOOKED)
                    .withMetadataFrom(event));
        } else {
            final JsonObject error = createObjectBuilder()
                    .add("code", provisionalBookingServiceResponse.getErrorCode().name())
                    .add("reason", provisionalBookingServiceResponse.getErrorMessage())
                    .build();
            sender.send(Enveloper.envelop(createObjectBuilder().add("error", error).build())
                    .withName(HEARING_SLOTS_UNCONFIRMED_BOOKING_FAILED)
                    .withMetadataFrom(event));
        }
    }
}
