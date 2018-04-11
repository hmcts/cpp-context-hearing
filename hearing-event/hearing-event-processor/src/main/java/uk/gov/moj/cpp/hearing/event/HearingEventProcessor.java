package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.listing.Hearing;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;
import uk.gov.moj.cpp.hearing.event.command.InitiateHearingCommand;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import javax.inject.Inject;
import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Inject
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;

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

    @Handles("hearing.hearing.confirmed-recorded")
    public void processHearingConfirmedRecorded(final JsonEnvelope event) {
        LOGGER.trace("Received hearing.hearing.confirmed-recorded event, processing");
        final JsonObject payload = event.payloadAsJsonObject();
        final Hearing hearing = this.jsonObjectToObjectConverter.convert(payload.getJsonObject(FIELD_HEARING), Hearing.class);
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));

        final InitiateHearingCommand initiateHearingCommand = getInitiateHearingCommand(caseId, hearing);

        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_INITIATE_HEARING)
                .apply(this.objectToJsonValueConverter.convert(initiateHearingCommand)));
    }



    @Handles("hearing.case.plea-added")
    public void processCasePleaAdded(final JsonEnvelope event) {
        LOGGER.trace("Received plea-added event, processing");
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_PLEA_ADD)
                .apply(event.payloadAsJsonObject()));

    }

    @Handles("hearing.case.plea-changed")
    public void processCasePleaChanged(final JsonEnvelope event) {

        LOGGER.trace("Received plea-changed event, processing");
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_PLEA_CHANGE)
                .apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.hearing-plea-updated")
    public void publishHearingPleaUpdatedPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-plea-updated' event received {}", payload);
        final String caseId = payload.getString(FIELD_CASE_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_PLEA_UPDATED).apply(Json.createObjectBuilder().add(FIELD_CASE_ID, caseId).build()));
    }
    @Handles("hearing.hearing-update-plea-ignored")
    public void publishHearingUpdatePleaIgnoredPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-update-plea-ignored' event received {}", payload);
        final String caseId = payload.getString(FIELD_CASE_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_UPDATE_PLEA_IGNORED).apply(Json.createObjectBuilder().add(FIELD_CASE_ID, caseId).build()));
    }

    @Handles("hearing.hearing-verdict-updated")
    public void publishHearingVerdictUpdatedPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-verdict-updated' event received {}", payload);
        final String hearingId = payload.getString(FIELD_HEARING_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_VERDICT_UPDATED).apply(Json.createObjectBuilder().add(FIELD_HEARING_ID, hearingId).build()));
    }

    @Handles("hearing.hearing-update-verdict-ignored")
    public void publishHearingUpdateVerdictIgnoredPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-update-verdict-ignored' event received {}", payload);
        final String hearingId = payload.getString(FIELD_HEARING_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_UPDATE_VERDICT_IGNORED).apply(Json.createObjectBuilder().add(FIELD_HEARING_ID, hearingId).build()));
    }

    @Handles("hearing.mags-court-hearing-recorded")
    public void magsCourtProcessed(final JsonEnvelope event) {
        LOGGER.trace("Received hearing.mags-court-hearing-recorded event, processing");
        final JsonObject payload = event.payloadAsJsonObject();
        final MagsCourtHearingRecorded preceding = this.jsonObjectToObjectConverter.convert(payload, MagsCourtHearingRecorded.class);
        final UUID caseId = preceding.getOriginatingHearing().getCaseId();

        final InitiateHearingCommand initiateHearingCommand =
                getInitiateHearingCommand(caseId, preceding.getOriginatingHearing(), preceding.getHearingId(), preceding.getConvictionDate());

        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_INITIATE_HEARING)
                .apply(this.objectToJsonValueConverter.convert(initiateHearingCommand)));

    }

    private InitiateHearingCommand getInitiateHearingCommand(final UUID caseId,
                                                             final Hearing hearing) {
        final InitiateHearingCommand command = new InitiateHearingCommand();
        command.setHearingId(hearing.getId());
        command.setCaseId(caseId);
        command.setCourtCentreId(hearing.getCourtCentreId() == null ? null : fromString(hearing.getCourtCentreId()));
        command.setRoomId(hearing.getCourtRoomId() == null ? null : fromString(hearing.getCourtRoomId()));
        command.setCourtCentreName(hearing.getCourtCentreName());
        command.setRoomName(hearing.getCourtRoomName());
        command.setDuration(hearing.getEstimateMinutes());
        command.setHearingType(hearing.getType());
        command.setStartDateTime(hearing.getStartDateTime());
        if (hearing.getJudge() != null) {
            command.setJudgeId(hearing.getJudge().getId());
            command.setJudgeFirstName(hearing.getJudge().getFirstName());
            command.setJudgeLastName(hearing.getJudge().getLastName());
            command.setJudgeTitle(hearing.getJudge().getTitle());
        }
        return command;
    }

    private InitiateHearingCommand getInitiateHearingCommand(final UUID caseId,
                                                             final uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing originatingHearing,
                                                             UUID newHearingId, LocalDate convictionDate) {
        //this is actually a create hearing command - the hearing may be historical
        final InitiateHearingCommand command = new InitiateHearingCommand();
        command.setHearingId(newHearingId);
        command.setCaseId(caseId);
        command.setCourtCentreId(originatingHearing.getCourtCentreId() == null ? null : fromString(originatingHearing.getCourtCentreId()));
        command.setCourtCentreName(originatingHearing.getCourtCentreName());
        command.setDuration(DEFAULT_HEARING_DURATION_MINUTES);
        command.setHearingType(originatingHearing.getType());
        command.setStartDateTime(convictionDate.atStartOfDay(ZoneOffset.UTC));
        return command;
    }

}
