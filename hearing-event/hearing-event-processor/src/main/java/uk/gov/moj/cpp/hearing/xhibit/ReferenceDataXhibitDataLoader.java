package uk.gov.moj.cpp.hearing.xhibit;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.*;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.service.EventMapping;
import uk.gov.moj.cpp.hearing.xhibit.pojo.CourtCentreCode;
import uk.gov.moj.cpp.hearing.xhibit.pojo.CourtCentreCourtList;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonValue;

@SuppressWarnings("squid:S1168")
@ApplicationScoped
public class ReferenceDataXhibitDataLoader implements ReferenceDataXhibitDataLoaderService {

    private static final String XHIBIT_COURT_MAPPINGS = "referencedata.query.cp-xhibit-court-mappings";
    private static final String XHIBIT_COURT_MAPPINGS_QUERY_PARAM = "oucode";

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Override
    public List<EventMapping> getEventMapping() {
        return null;
    }


    @Override
    public CourtCentreCode getXhibitCourtCentreCodeBy(final JsonEnvelope jsonEnvelope, final String courtCentreId) {

        final JsonValue query = createObjectBuilder().add(XHIBIT_COURT_MAPPINGS_QUERY_PARAM, courtCentreId).build();

        final Envelope<JsonValue> jsonValueEnvelope = envelop(query)
                .withName(XHIBIT_COURT_MAPPINGS)
                .withMetadataFrom(jsonEnvelope);

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(envelopeFrom(jsonEnvelope.metadata(), jsonValueEnvelope.payload()));

        final CourtCentreCourtList courtCentreCourtList = jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), CourtCentreCourtList.class);
        return courtCentreCourtList.getCpXhibitCourtMappings().get(0);
    }
}
