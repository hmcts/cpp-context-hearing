package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.referencedata.HearingTypeMappingList;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import com.google.common.annotations.VisibleForTesting;

public class ReferenceDataService {
    private static final String GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES = "referencedata.query.cracked-ineffective-vacated-trial-types";
    private static final String REFERENCEDATA_QUERY_COURT_CENTRES = "referencedata.query.courtrooms";
    private static final String XHIBIT_EVENT_MAPPINGS = "referencedata.query.cp-xhibit-hearing-event-mappings";
    private static final String REFERENCE_DATA_HEARING_TYPES = "referencedata.query.hearing-types";

    @Inject
    @ServiceComponent(QUERY_VIEW)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private UtcClock utcClock;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public CrackedIneffectiveVacatedTrialTypes getCrackedIneffectiveVacatedTrialTypes() {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder().
                        withId(randomUUID()).
                        withName(GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES),
                createObjectBuilder());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), CrackedIneffectiveVacatedTrialTypes.class);
    }

    public XhibitEventMappingsList getEventMapping() {
        final Metadata metadata = JsonEnvelope.metadataBuilder()
                .createdAt(utcClock.now())
                .withName(XHIBIT_EVENT_MAPPINGS)
                .withId(randomUUID())
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, Json.createObjectBuilder().build());

        return requester.requestAsAdmin(jsonEnvelope, XhibitEventMappingsList.class).payload();
    }
    public JsonEnvelope getCourtRooms(final JsonEnvelope eventEnvelope) {
        final JsonObject payload = createObjectBuilder()
                .add("oucode", "C")
                .build();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(eventEnvelope, REFERENCEDATA_QUERY_COURT_CENTRES).apply(payload);
        return requester.request(requestEnvelope);
    }

    public HearingTypeMappingList getXhibitHearingType() {
        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder().
                        withId(randomUUID()).
                        withName(REFERENCE_DATA_HEARING_TYPES),
                createObjectBuilder());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), HearingTypeMappingList.class);
    }

    @VisibleForTesting
    void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }
}
