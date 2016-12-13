package uk.gov.moj.cpp.hearing.query.view;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonString;

@ServiceComponent(Component.QUERY_VIEW)
public class HearingQueryView {

    static final String FIELD_HEARING_ID = "hearingId";
    static final String FIELD_START_DATE = "startDate";
    static final String NAME_RESPONSE_HEARING_LIST = "hearing.get.hearings-by-startdate-response";
    static final String NAME_RESPONSE_HEARING = "hearing.get.hearing-response";

    @Inject
    private Requester requester;

    @Inject
    private ListToJsonArrayConverter helperService;

    @Inject
    private HearingService hearingService;

    @Inject
    private Enveloper enveloper;

    @Handles("hearing.get.hearings-by-startdate")
    public JsonEnvelope findHearings(final JsonEnvelope envelope) {
        Optional<JsonString> startDate =
                JsonObjects.getJsonString(envelope.payloadAsJsonObject(), FIELD_START_DATE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate localDate = LocalDate.parse(startDate.get().getChars(),formatter);
        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING_LIST)
                .apply(Json.createObjectBuilder().add("hearings",
                        helperService.convert(hearingService.getHearingsByStartDate(
                                localDate)))
                        .build());
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope envelope) {
        Optional<UUID> hearingId =
                JsonObjects.getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING)
                .apply(hearingService.getHearingById(hearingId.get()));
    }
}
