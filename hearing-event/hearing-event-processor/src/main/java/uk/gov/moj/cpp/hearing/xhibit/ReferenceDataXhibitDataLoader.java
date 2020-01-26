package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.json.JsonObject.NULL;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

@SuppressWarnings("squid:S1168")
@ApplicationScoped
public class ReferenceDataXhibitDataLoader {

    private static final String XHIBIT_COURT_ROOM_MAPPINGS = "referencedata.query.cp-xhibit-courtroom-mappings";
    private static final String XHIBIT_EVENT_MAPPINGS = "referencedata.query.cp-xhibit-hearing-event-mappings";
    private static final String XHIBIT_COURT_MAPPINGS_QUERY_PARAM = "ouId";

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    @Inject
    private UtcClock utcClock;

    public XhibitEventMappingsList getEventMapping() {
        final Metadata metadata = metadataBuilder()
                .createdAt(utcClock.now())
                .withName(XHIBIT_EVENT_MAPPINGS)
                .withId(randomUUID())
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, Json.createObjectBuilder().build());

        return requester.requestAsAdmin(jsonEnvelope, XhibitEventMappingsList.class).payload();
    }

    public String getXhibitCourtCentreCodeBy(final String courtCentreId) {
        final CourtRoomMappingsList courtRoomMappingsList = getCourtRoomMappingsList(courtCentreId);
        return courtRoomMappingsList.getCpXhibitCourtRoomMappings().get(0).getCrestCourtSiteCode();
    }

    private CourtRoomMappingsList getCourtRoomMappingsList(final String courtCentreId) {
        final JsonObject query = createObjectBuilder()
                .add(XHIBIT_COURT_MAPPINGS_QUERY_PARAM, courtCentreId)
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataBuilder()
                        .createdAt(utcClock.now())
                        .withName(XHIBIT_COURT_ROOM_MAPPINGS)
                        .withId(randomUUID())
                        .build(),
                query);

        return requester.requestAsAdmin(jsonEnvelope, CourtRoomMappingsList.class).payload();
    }
}

