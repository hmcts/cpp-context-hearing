package uk.gov.moj.cpp.hearing.query.view;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.service.OffenceService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

@ServiceComponent(Component.QUERY_VIEW)
public class OffenceQueryView {
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String RESPONSE_NAME = "hearing.get.offences";


    @Inject
    private OffenceService offenceService;

    @Inject
    private Enveloper enveloper;

    @Handles("hearing.get.offences")
    public JsonEnvelope getOffences(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        return this.enveloper.withMetadataFrom(envelope, RESPONSE_NAME).apply(
                offenceService.getOffencesByHearingId(hearingId.get())
        );

    }
}
