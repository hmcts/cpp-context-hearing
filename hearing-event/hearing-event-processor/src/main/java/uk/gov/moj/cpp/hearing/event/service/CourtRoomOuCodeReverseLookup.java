package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.concat;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.hearing.courts.referencedata.CourtroomOuCode;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

public class CourtRoomOuCodeReverseLookup {

    public static final String GET_OU_COURT_CODE = "referencedata.query.get.police-opt-courtroom-ou-courtroom-code";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public String getcourtRoomOuCode(final JsonEnvelope context, final Integer courtRoomID, final String oucode) {
        return courtRoomOuCode(context, courtRoomID, oucode);
    }

    private String courtRoomOuCode(final JsonEnvelope context, final Integer courtRoomID, final String oucode) {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataFrom(context.metadata()).withName(GET_OU_COURT_CODE).build(),
                createObjectBuilder().add("courtRoomId", courtRoomID.toString()).build());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        final JsonObject json = jsonResultEnvelope.payloadAsJsonObject();
        final CourtroomOuCode courtroomOuCode = jsonObjectToObjectConverter.convert(json, CourtroomOuCode.class);

        final Stream<String> ouCourtRoomCodes = ofNullable(courtroomOuCode.getOuCourtRoomCodes()).map(Collection::stream)
                .orElseGet(Stream::empty);

        final Stream<String> ouCodes = ofNullable(courtroomOuCode.getOuCodes()).map(Collection::stream)
                .orElseGet(Stream::empty);

        return concat(ouCourtRoomCodes, ouCodes)
                .filter(x -> x.startsWith(oucode.substring(0, 5)))
                .findFirst()
                .orElse(EMPTY);
    }
}
