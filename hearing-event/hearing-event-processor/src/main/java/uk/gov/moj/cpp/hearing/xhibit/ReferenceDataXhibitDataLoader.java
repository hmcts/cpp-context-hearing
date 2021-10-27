package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.common.exception.ReferenceDataNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@SuppressWarnings("squid:S1168")
@ApplicationScoped
public class ReferenceDataXhibitDataLoader {

    private static final String XHIBIT_EVENT_MAPPINGS = "referencedata.query.cp-xhibit-hearing-event-mappings";

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

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, createObjectBuilder().build());

        final XhibitEventMappingsList payload = requester.requestAsAdmin(jsonEnvelope, XhibitEventMappingsList.class).payload();

        if (payload == null || isEmpty(payload.getCpXhibitHearingEventMappings())) {
            throw new ReferenceDataNotFoundException(jsonEnvelope, "xhibit event mapping not found");
        }

        return payload;
    }
}

