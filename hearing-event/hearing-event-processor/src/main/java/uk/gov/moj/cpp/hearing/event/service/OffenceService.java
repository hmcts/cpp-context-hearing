package uk.gov.moj.cpp.hearing.event.service;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.OffenceBailStatusResponse;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class OffenceService {
    private static final String HEARING_QUERY_OFFENCE_BAIL_STATUS_FOR_DEFENDANT = "hearing.offence-bail-status-for-defendant";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    public List<OffenceBailStatus> getOffenceBailStatus(final UUID defendantId) {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withId(UUID.randomUUID()).withName(HEARING_QUERY_OFFENCE_BAIL_STATUS_FOR_DEFENDANT).build(),
                createObjectBuilder().add("defendantId", defendantId.toString()).build());

        return requester.requestAsAdmin(requestEnvelope, OffenceBailStatusResponse.class).payload().getOffenceBailStatuses();
    }
}
