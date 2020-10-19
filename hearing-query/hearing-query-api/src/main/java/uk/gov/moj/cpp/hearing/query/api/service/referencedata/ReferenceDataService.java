package uk.gov.moj.cpp.hearing.query.api.service.referencedata;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataService.class);
    private static final String GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES = "referencedata.query.cracked-ineffective-vacated-trial-types";
    private static final String XHIBIT_EVENT_MAPPINGS = "referencedata.query.cp-xhibit-hearing-event-mappings";
    private static final String REFERENCEDATA_QUERY_COURT_CENTRES = "referencedata.query.courtrooms";

    @Inject
    @ServiceComponent(QUERY_API)
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

        final Envelope<JsonObject> jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope, JsonObject.class);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payload(), CrackedIneffectiveVacatedTrialTypes.class);
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

    public JsonObject getAllCourtRooms(final JsonEnvelope eventEnvelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempting to get all court rooms...");
        }

        final JsonObject payload = createObjectBuilder()
                .build();

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(payload)
                .withName(REFERENCEDATA_QUERY_COURT_CENTRES)
                .withMetadataFrom(eventEnvelope);

        return requester.requestAsAdmin(requestEnvelope, JsonObject.class).payload();
    }

}
