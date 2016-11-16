package uk.gov.moj.cpp.hearing.query.view;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

@ServiceComponent(Component.QUERY_VIEW)
public class HearingQueryView {

    static final String FIELD_CASE_ID = "caseid";
    static final String FIELD_HEARING_ID = "hearingid";
    static final String FIELD_FROM_DATE = "fromdate";
    static final String FIELD_HEARING_TYPE = "hearingtype";
    static final String NAME_RESPONSE_HEARING_LIST = "hearing.query.case-hearings-response";
    static final String NAME_RESPONSE_HEARING = "hearing.query.hearing-response";

    @Inject
    private Requester requester;

    @Inject
    private ListToJsonArrayConverter helperService;

    @Inject
    private HearingService hearingService;

    @Inject
    private Enveloper enveloper;

    @Handles("hearing.query.hearings")
    public JsonEnvelope findHearings(final JsonEnvelope envelope) {
        Optional<UUID> caseId = JsonObjects.getUUID(envelope.payloadAsJsonObject(), FIELD_CASE_ID);
        Optional<String> fromDate =
                        JsonObjects.getString(envelope.payloadAsJsonObject(), FIELD_FROM_DATE);
        Optional<String> hearingType =
                        JsonObjects.getString(envelope.payloadAsJsonObject(), FIELD_HEARING_TYPE);

        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING_LIST)
                        .apply(Json.createObjectBuilder().add("hearings",
                                        helperService.convert(hearingService.getHearingsForCase(
                                                        caseId.get(), fromDate, hearingType)))
                                        .build());
    }

    @Handles("hearing.query.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope envelope) {
        Optional<UUID> hearingId =
                        JsonObjects.getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING)
                        .apply(hearingService.getHearingById(hearingId.get()));
    }
}
