package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ShareResultsFailed;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventTrialVacated;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:CallToDeprecatedMethod")
@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    public static final String PUBLIC_HEARING_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";
    public static final String PUBLIC_HEARING_MULTIPLE_DRAFT_RESULTS_SAVED = "public.hearing.multiple-draft-results-saved";
    public static final String PUBLIC_HEARING_SAVE_DRAFT_RESULT_FAILED = "public.hearing.save-draft-result-failed";
    public static final String PUBLIC_HEARING_SHARE_RESULTS_FAILED = "public.hearing.share-results-failed";
    public static final String PUBLIC_HEARING_TRIAL_VACATED = "public.hearing.trial-vacated";
    public static final String PUBLIC_LISTING_HEARING_RESCHEDULED = "public.listing.hearing-rescheduled";
    public static final String COMMAND_LISTING_HEARING_RESCHEDULED = "hearing.command.clear-vacated-trial";

    public static final String PUBLIC_HEARING_EVENT_AMENDED = "public.hearing.event-amended";
    public static final String PUBLIC_HEARING_DRAFT_RESULT_DELETED_V2 = "public.hearing.draft-result-deleted-v2";


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

    @Handles("hearing.event.amended")
    public void publicHearingAmended(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.amended event received {}", event.toObfuscatedDebugString());
        }

        final HearingAmended hearingAmended = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingAmended.class);

        final PublicHearingAmended publicHearingAmended = PublicHearingAmended.publicHearingAmended()
                .setHearingId(hearingAmended.getHearingId())
                .setNewHearingState(hearingAmended.getNewHearingState());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingAmended);
        this.sender.send(envelopeFrom(metadataFrom(event.metadata()).withName(PUBLIC_HEARING_EVENT_AMENDED), publicEventPayload));
    }

    @Handles("hearing.draft-result-saved")
    public void publicDraftResultSavedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-saved event received {}", event.toObfuscatedDebugString());
        }

        final Target target = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultSaved.class).getTarget();

        final PublicHearingDraftResultSaved publicHearingDraftResultSaved = PublicHearingDraftResultSaved.publicHearingDraftResultSaved()
                .setHearingId(target.getHearingId())
                .setDefendantId(target.getDefendantId())
                .setOffenceId(target.getOffenceId())
                .setTargetId(target.getTargetId());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingDraftResultSaved);

       if(LOGGER.isErrorEnabled())  {LOGGER.error("raising public event hearing.draft-result-saved correlationId: {}" , event.metadata().clientCorrelationId().orElse(null));}
        this.sender.send(envelopeFrom(metadataFrom(event.metadata()).withName(PUBLIC_HEARING_DRAFT_RESULT_SAVED), publicEventPayload));
        if(LOGGER.isErrorEnabled()) {LOGGER.error("raised public event hearing.draft-result-saved correlationId: {}" , event.metadata().clientCorrelationId().orElse(null));}
    }

    @Handles("hearing.draft-result-saved-v2")
    public void publicDraftResultSavedV2PublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-saved-v2 event received {}", event.toObfuscatedDebugString());
        }

        final UUID hearingId = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultSavedV2.class).getHearingId();

        final PublicHearingDraftResultSaved publicHearingDraftResultSaved = PublicHearingDraftResultSaved.publicHearingDraftResultSaved()
                .setHearingId(hearingId);

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingDraftResultSaved);
        if(LOGGER.isErrorEnabled())  {LOGGER.error("raising public event hearing.draft-result-saved-v2 correlationId: {}" , event.metadata().clientCorrelationId().orElse(null));}
        this.sender.send(envelopeFrom(metadataFrom(event.metadata()).withName(PUBLIC_HEARING_DRAFT_RESULT_SAVED), publicEventPayload));
        if(LOGGER.isErrorEnabled()) {LOGGER.error("raised public event hearing.draft-result-saved-v2 correlationId: {}" , event.metadata().clientCorrelationId().orElse(null));}
    }

    @Handles("hearing.draft-result-deleted-v2")
    public void publicDraftResultDeletedV2PublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-deleted-v2 event received {}", event.toObfuscatedDebugString());
        }

        final DraftResultDeletedV2 draftResultDeletedV2 = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultDeletedV2.class);

        final PublicHearingDraftResultDeleted publicHearingDraftResultDeleted = PublicHearingDraftResultDeleted.publicHearingDraftResultSaved()
                .setHearingId(draftResultDeletedV2.getHearingId())
                .setHaringDay(draftResultDeletedV2.getHearingDay());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingDraftResultDeleted);
        this.sender.send(envelopeFrom(metadataFrom(event.metadata()).withName(PUBLIC_HEARING_DRAFT_RESULT_DELETED_V2), publicEventPayload));
    }


    @Handles("hearing.save-draft-result-failed")
    public void handleSaveDraftResultFailedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.save-draft-result-failed event received {}", event.toObfuscatedDebugString());
        }

        final Target target = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), SaveDraftResultFailed.class).getTarget();

        final PublicHearingSaveDraftResultFailed publicEventSaveDraftResultFailed = PublicHearingSaveDraftResultFailed.publicHearingSaveDraftResultFailed()
                .setHearingId(target.getHearingId())
                .setDefendantId(target.getDefendantId())
                .setOffenceId(target.getOffenceId())
                .setTargetId(target.getTargetId());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicEventSaveDraftResultFailed);

        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_SAVE_DRAFT_RESULT_FAILED).apply(publicEventPayload));
    }

    @Handles("hearing.share-results-failed")
    public void handleShareResultsFailedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.share-results-failed event received {}", event.toObfuscatedDebugString());
        }

        final ShareResultsFailed shareResultsFailed = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ShareResultsFailed.class);

        final PublicHearingShareResultsFailed publicEventShareResultsFailed = PublicHearingShareResultsFailed.publicHearingShareResultsFailed()
                .setHearingId(shareResultsFailed.getHearingId())
                .setHearingState(shareResultsFailed.getHearingState())
                .setAmendedByUserId(shareResultsFailed.getAmendedByUserId());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicEventShareResultsFailed);
        final MetadataBuilder metadata = metadataFrom(event.metadata()).withName(PUBLIC_HEARING_SHARE_RESULTS_FAILED);
        sender.send(envelopeFrom(metadata, publicEventPayload));

    }

    @Handles("hearing.multiple-draft-results-saved")
    public void handleMultipleDraftResultFailedEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.multiple-draft-results-saved {}", event.toObfuscatedDebugString());
        }
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_MULTIPLE_DRAFT_RESULTS_SAVED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.hearing-effective-trial-set")
    public void publicHearingEventEffectiveTrialSetPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-effective-trial-type-set event received {}", event.toObfuscatedDebugString());
        }

        final HearingEffectiveTrial hearingEffectiveTrial = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingEffectiveTrial.class);

        final PublicHearingEventTrialVacated publicHearingEventTrialVacated = PublicHearingEventTrialVacated.publicHearingEventTrialVacated()
                .setHearingId(hearingEffectiveTrial.getHearingId())
                .setVacatedTrialReasonId(null);


        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingEventTrialVacated);

        final MetadataBuilder metadata = metadataFrom(event.metadata()).withName(PUBLIC_HEARING_TRIAL_VACATED);
        sender.send(envelopeFrom(metadata, publicEventPayload));

    }

    @Handles("hearing.hearing-trial-type-set")
    public void publicHearingEventTrialTypeSetPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-trial-type-set event received {}", event.toObfuscatedDebugString());
        }

        final HearingTrialType hearingTrialType = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingTrialType.class);


        final PublicHearingEventTrialVacated publicHearingEventTrialVacated = PublicHearingEventTrialVacated.publicHearingEventTrialVacated()
                .setHearingId(hearingTrialType.getHearingId())
                .setVacatedTrialReasonId(null);


        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingEventTrialVacated);

        final MetadataBuilder metadata = metadataFrom(event.metadata()).withName(PUBLIC_HEARING_TRIAL_VACATED);
        sender.send(envelopeFrom(metadata, publicEventPayload));

    }

    @Handles("hearing.trial-vacated")
    public void publicHearingEventVacateTrialTypeSetPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.trial-vacated event received {}", event.toObfuscatedDebugString());
        }

        final HearingTrialVacated hearingTrialType = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingTrialVacated.class);

        final PublicHearingEventTrialVacated publicHearingEventTrialVacated = PublicHearingEventTrialVacated.publicHearingEventTrialVacated()
                .setHearingId(hearingTrialType.getHearingId())
                .setVacatedTrialReasonId(hearingTrialType.getVacatedTrialReasonId());

        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(publicHearingEventTrialVacated);

        final MetadataBuilder metadata = metadataFrom(event.metadata()).withName(PUBLIC_HEARING_TRIAL_VACATED);
        sender.send(envelopeFrom(metadata, publicEventPayload));
    }

    @Handles(PUBLIC_LISTING_HEARING_RESCHEDULED)
    public void handlePublicListingHearingRescheduled(final JsonEnvelope envelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.hearing.trial-vacated event received {}", envelope.toObfuscatedDebugString());
        }

        this.sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName(COMMAND_LISTING_HEARING_RESCHEDULED),
                envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.hearing-days-cancelled")
    public void handleHearingDaysCancelled(final JsonEnvelope envelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-days-cancelled {}", envelope.toObfuscatedDebugString());
        }

        this.sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName("public.hearing.hearing-days-cancelled"),
                envelope.payloadAsJsonObject()));
    }
}
