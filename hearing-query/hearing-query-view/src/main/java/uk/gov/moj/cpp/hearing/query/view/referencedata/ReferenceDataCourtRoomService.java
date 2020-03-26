package uk.gov.moj.cpp.hearing.query.view.referencedata;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMappingsList;

import java.util.Objects;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

@ApplicationScoped
public class ReferenceDataCourtRoomService {

    private static final String XHIBIT_COURT_ROOM_MAPPINGS = "referencedata.query.cp-xhibit-courtroom-mappings";
    private static final String XHIBIT_COURT_MAPPINGS_QUERY_PARAM = "ouId";

    @ServiceComponent(QUERY_VIEW)
    @Inject
    private Requester requester;

    @Inject
    private UtcClock utcClock;

    public CourtRoomMapping getCourtRoomNameBy(final UUID courtCentreId, final UUID courtRoomId) {

        final CourtRoomMappingsList courtRoomMappingsList = getCourtRoomMappingsList(courtCentreId.toString());

        return courtRoomMappingsList
                .getCpXhibitCourtRoomMappings()
                .stream()
                .filter(courtRoomMappings -> Objects.nonNull(courtRoomMappings.getCourtRoomUUID()))
                .filter(courtRoomMappings -> courtRoomMappings.getCourtRoomUUID().equals(courtRoomId))
                .findFirst()
                .orElseThrow(() ->
                        new ExhibitReferenceDataException(format("Unable to find exhibit court room name for CPP court centre id: %s and CPP court room id: %s", courtCentreId.toString(), courtRoomId.toString())));
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

        return requester
                .requestAsAdmin(jsonEnvelope, CourtRoomMappingsList.class)
                .payload();
    }
}
