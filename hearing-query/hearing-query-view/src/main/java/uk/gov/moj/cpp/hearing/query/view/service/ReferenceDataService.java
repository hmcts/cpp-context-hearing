package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;

import javax.inject.Inject;
import javax.json.Json;

public class ReferenceDataService {
    private static final String GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES = "referencedata.query.cracked-ineffective-vacated-trial-types";
    private static final String XHIBIT_EVENT_MAPPINGS = "referencedata.query.cp-xhibit-hearing-event-mappings";

    @Inject
    @ServiceComponent(QUERY_VIEW)
    private Requester requester;

    @Inject
    private UtcClock utcClock;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public CrackedIneffectiveVacatedTrialTypes listAllCrackedIneffectiveVacatedTrialTypes() {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder().
                        withId(randomUUID()).
                        withName(GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES),
                createObjectBuilder());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), CrackedIneffectiveVacatedTrialTypes.class);
    }

    public XhibitEventMappingsList listAllEventMappings() {
        final Metadata metadata = JsonEnvelope.metadataBuilder()
                .createdAt(utcClock.now())
                .withName(XHIBIT_EVENT_MAPPINGS)
                .withId(randomUUID())
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, Json.createObjectBuilder().build());

        return requester.requestAsAdmin(jsonEnvelope, XhibitEventMappingsList.class).payload();
    }

}
