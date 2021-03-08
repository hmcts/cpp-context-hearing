package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingStatesEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingStatesEventProcessor.class);
    public static final String HEARING_EVENT_APPROVAL_REJECTED = "hearing.event.approval-rejected";
    public static final String HEARING_HEARING_LOCKED = "hearing.event.hearing-locked";
    public static final String HEARING_HEARING_LOCKED_BY_OTHER_USER = "hearing.event.hearing-locked-by-other-user";
    public static final String HEARING_EVENT_APPROVAL_REQUESTED = "hearing.event.approval-requested";
    public static final String HEARING_EVENT_RESULT_AMENDMENTS_VALIDATION_FAILED = "hearing.event.result-amendments-validation-failed";

    public static final String PUBLIC_HEARING_HEARING_LOCKED_BY_OTHER_USER = "public.hearing.hearing-locked-by-other-user";
    public static final String PUBLIC_HEARING_HEARING_LOCKED = "public.hearing.hearing-locked";
    public static final String PUBLIC_HEARING_EVENT_APPROVAL_REJECTED = "public.hearing.approval-rejected";
    public static final String PUBLIC_HEARING_APPROVAL_REQUESTED = "public.hearing.approval-requested";
    public static final String PUBLIC_HEARING_EVENT_RESULT_AMENDMENTS_VALIDATION_FAILED = "public.hearing.result-amendments-validation-failed";

    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Transactional
    @Handles(HEARING_EVENT_APPROVAL_REJECTED)
    public void approvalRejected(final JsonEnvelope envelope) {
        log(PUBLIC_HEARING_EVENT_APPROVAL_REJECTED, envelope);
        this.sender.send(envelopeFrom(
                metadataFrom(envelope.metadata()).withName(PUBLIC_HEARING_EVENT_APPROVAL_REJECTED),
                envelope.payloadAsJsonObject()));
    }

    @Transactional
    @Handles(HEARING_HEARING_LOCKED)
    public void hearingLocked(final JsonEnvelope envelope) {
        log(PUBLIC_HEARING_HEARING_LOCKED, envelope);
        this.sender.send(envelopeFrom(
                metadataFrom(envelope.metadata()).withName(PUBLIC_HEARING_HEARING_LOCKED),
                envelope.payloadAsJsonObject()));
    }

    @Transactional
    @Handles(HEARING_HEARING_LOCKED_BY_OTHER_USER)
    public void hearingLockedByOtherUser(final JsonEnvelope envelope) {
        log(PUBLIC_HEARING_HEARING_LOCKED_BY_OTHER_USER, envelope);
        this.sender.send(envelopeFrom(
                metadataFrom(envelope.metadata()).withName(PUBLIC_HEARING_HEARING_LOCKED_BY_OTHER_USER),
                envelope.payloadAsJsonObject()));
    }



    @Handles(HEARING_EVENT_APPROVAL_REQUESTED)
    public void processApprovalRequested(final JsonEnvelope envelope) {
        log(HEARING_EVENT_APPROVAL_REQUESTED, envelope);
        this.sender.send(envelopeFrom(
                metadataFrom(envelope.metadata()).withName(PUBLIC_HEARING_APPROVAL_REQUESTED),
                envelope.payloadAsJsonObject()));
    }

    @Handles(HEARING_EVENT_RESULT_AMENDMENTS_VALIDATION_FAILED)
    public void processResultAmendmentsValidationFailed(final JsonEnvelope envelope) {
        log(HEARING_EVENT_APPROVAL_REQUESTED, envelope);
        this.sender.send(envelopeFrom(
                metadataFrom(envelope.metadata()).withName(PUBLIC_HEARING_EVENT_RESULT_AMENDMENTS_VALIDATION_FAILED),
                envelope.payloadAsJsonObject()));
    }

    private void log(final String msg, final JsonEnvelope envelope){
        LOGGER.info("{} emitted for {}", msg, envelope.payloadAsJsonObject());
    }
}