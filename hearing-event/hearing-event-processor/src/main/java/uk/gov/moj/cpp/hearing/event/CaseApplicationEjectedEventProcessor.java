package uk.gov.moj.cpp.hearing.event;

import static java.util.Objects.isNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
@SuppressWarnings("squid:CallToDeprecatedMethod")
public class CaseApplicationEjectedEventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseApplicationEjectedEventProcessor.class);
    private static final String HEARING_COMMAND_EJECT_CASE_OR_APPLICATION = "hearing.command.eject-case-or-application";
    private static final String EVENT_RECEIVED_LOG_TEMPLATE = "{} event received {}";
    private static final String PUBLIC_EVENT_CASE_OR_APPLICATION_EJECTED = "public.progression.events.case-or-application-ejected";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles(PUBLIC_EVENT_CASE_OR_APPLICATION_EJECTED)
    public void processCaseApplicationEjected(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_RECEIVED_LOG_TEMPLATE, PUBLIC_EVENT_CASE_OR_APPLICATION_EJECTED, payload);
        }
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_COMMAND_EJECT_CASE_OR_APPLICATION).apply(event.payloadAsJsonObject()));

    }
}
