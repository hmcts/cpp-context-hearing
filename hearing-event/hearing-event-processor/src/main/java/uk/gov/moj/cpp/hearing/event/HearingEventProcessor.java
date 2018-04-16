package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventProcessor.class);

    private static final String PUBLIC_HEARING_RESULTED = "public.hearing.resulted";
    private static final String PUBLIC_HEARING_RESULT_AMENDED = "public.hearing.result-amended";
    private static final String PUBLIC_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";
    private static final String PUBLIC_HEARING_PLEA_UPDATED = "public.hearing.plea-updated";
    private static final String PUBLIC_HEARING_VERDICT_UPDATED = "public.hearing.verdict-updated";
    private static final String PUBLIC_HEARING_UPDATE_PLEA_IGNORED = "public.hearing.update-plea-ignored";
    private static final String PUBLIC_HEARING_UPDATE_VERDICT_IGNORED = "public.hearing.update-verdict-ignored";

    private static final String FIELD_HEARING = "hearing";

    private static final String HEARING_INITIATE_HEARING = "hearing.initiate-hearing";
    private static final String HEARING_RECORD_MAGS_COURT_HEARING = "hearing.record-mags-court-hearing";
    private static final String HEARING_PLEA_ADD = "hearing.plea-add";
    private static final String HEARING_PLEA_CHANGE = "hearing.plea-change";

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_CASE_ID = "caseId";
    private static final int DEFAULT_HEARING_DURATION_MINUTES = 15;

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;


    @Handles("hearing.results-shared")
    public void publishHearingResultsSharedPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.debug("'public.hearing.resulted' event received {}", payload);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULTED).apply(payload));
    }

    @Handles("hearing.result-amended")
    public void publishHearingResultAmendedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULT_AMENDED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.draft-result-saved")
    public void publicDraftResultSavedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_DRAFT_RESULT_SAVED).apply(event.payloadAsJsonObject()));
    }

}
