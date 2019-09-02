package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    public static final String PUBLIC_HEARING_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";

    public static final String PUBLIC_HEARING_APPLICATION_DRAFT_RESULTED = "public.hearing.application-draft-resulted";
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventProcessor.class);
    private final Enveloper enveloper;
    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public HearingEventProcessor(final Enveloper enveloper, final Sender sender, final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                 final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    @Handles("hearing.draft-result-saved")
    public void publicDraftResultSavedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-saved event received {}", event.toObfuscatedDebugString());
        }

        final Target target = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultSaved.class).getTarget();

        final PublicHearingDraftResultSaved publicHearingDraftResultSaved = PublicHearingDraftResultSaved.publicHearingDraftResultSaved()
                .setDraftResult(target.getDraftResult())
                .setHearingId(target.getHearingId())
                .setDefendantId(target.getDefendantId())
                .setOffenceId(target.getOffenceId())
                .setTargetId(target.getTargetId());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingDraftResultSaved);

        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_DRAFT_RESULT_SAVED).apply(publicEventPayload));
    }

    @Handles("hearing.application-draft-resulted")
    public void publicApplicationDraftResultedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.application-draft-resulted event received {}", event.toObfuscatedDebugString());
        }

        final ApplicationDraftResulted applicationDraftResulted = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ApplicationDraftResulted.class);

        final PublicHearingApplicationDraftResulted publicHearingApplicationDraftResulted = PublicHearingApplicationDraftResulted.publicHearingApplicationDraftResulted()
                .setDraftResult(applicationDraftResulted.getDraftResult())
                .setHearingId(applicationDraftResulted.getHearingId())
                .setApplicationId(applicationDraftResulted.getApplicationId())
                .setTargetId(applicationDraftResulted.getTargetId())
                .setApplicationOutcomeType(applicationDraftResulted.getApplicationOutcomeType())
                .setApplicationOutcomeDate(applicationDraftResulted.getApplicationOutcomeDate());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingApplicationDraftResulted);

        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_APPLICATION_DRAFT_RESULTED).apply(publicEventPayload));
    }

}
