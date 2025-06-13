package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import javax.json.Json;
import javax.json.JsonObject;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class ApplicationDetailChangeEventProcessor {

    @Inject
    private Logger LOGGER;
    private static final String COURT_APPLICATION = "courtApplication";

    private static final String COMMAND_UPDATE_COURT_APPLICATION = "hearing.update-court-application";
    private static final String COMMAND_UPDATE_LAA_REFERENCE_FOR_APPLICATION = "hearing.update-laareference-for-application";
    private static final String COMMAND_UPDATE_DEFENCE_ORGANISATION_FOR_APPLICATION = "hearing.update-defence-organisation-for-application";
    private static final String PUBLIC_EVENT_PROGRESSION_COURT_APPLICATION_CHANGED = "public.progression.court-application-updated";
    private static final String PUBLIC_EVENT_PROGRESSION_APPLICATION_OFFENCES_UPDATED = "public.progression.application-offences-updated";
    private static final String PUBLIC_EVENT_PROGRESSION_APPLICATION_ORGANISATION_CHANGED = "public.progression.application-organisation-changed";
    private static final String EVENT_RECEIVED_LOG_TEMPLATE = "{} event received {}";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;



    @Handles(PUBLIC_EVENT_PROGRESSION_COURT_APPLICATION_CHANGED)
    public void handleCourtApplicationChanged(final JsonEnvelope envelope) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_RECEIVED_LOG_TEMPLATE, PUBLIC_EVENT_PROGRESSION_COURT_APPLICATION_CHANGED, envelope.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(envelope, COMMAND_UPDATE_COURT_APPLICATION).apply(envelope.payloadAsJsonObject()));
    }


    @Handles(PUBLIC_EVENT_PROGRESSION_APPLICATION_OFFENCES_UPDATED)
    public void handleApplicationOffenceUpdated(final JsonEnvelope envelope) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_RECEIVED_LOG_TEMPLATE, PUBLIC_EVENT_PROGRESSION_APPLICATION_OFFENCES_UPDATED, envelope.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(envelope, COMMAND_UPDATE_LAA_REFERENCE_FOR_APPLICATION).apply(envelope.payloadAsJsonObject()));
    }

    @Handles(PUBLIC_EVENT_PROGRESSION_APPLICATION_ORGANISATION_CHANGED)
    public void handleApplicationDefenceOrganisationUpdated(final JsonEnvelope envelope) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_RECEIVED_LOG_TEMPLATE, PUBLIC_EVENT_PROGRESSION_APPLICATION_ORGANISATION_CHANGED, envelope.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(envelope, COMMAND_UPDATE_DEFENCE_ORGANISATION_FOR_APPLICATION).apply(envelope.payloadAsJsonObject()));
    }
}
