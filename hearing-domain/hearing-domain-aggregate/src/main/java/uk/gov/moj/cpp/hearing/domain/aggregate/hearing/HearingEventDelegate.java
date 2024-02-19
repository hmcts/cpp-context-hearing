package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.domain.CourtCentre;
import uk.gov.moj.cpp.hearing.domain.HearingType;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingEventDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing event not found";
    private static final String REASON_HEARING_NOT_FOUND = "Hearing not found";
    private static final String REASON_CASEURN_NOT_FOUND = "Case URN not found";
    private static final String REASON_EVENT_LOG_NOT_ALLOWED_FOR_BOX_HEARING = "Hearing Event Log not allowed for Box Hearing";



    private final HearingAggregateMomento momento;

    public HearingEventDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleHearingEventLogged(HearingEventLogged hearingEventLogged) {
        this.momento.getHearingEvents().put(hearingEventLogged.getHearingEventId(), new HearingEvent(hearingEventLogged));
    }

    public void handleHearingEventDeleted(HearingEventDeleted hearingEventDeleted) {
        this.momento.getHearingEvents().get(hearingEventDeleted.getHearingEventId()).setDeleted(true);
    }

    public Stream<Object> logHearingEvent(final UUID hearingId, final UUID hearingEventDefinitionId, final Boolean alterable, final UUID defenceCounselId, final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent, final UUID userId) {

        Optional<String> ignoreReason = validateHearingEvent(hearingEvent.getHearingEventId());
        if (ignoreReason.isPresent()) {
            return Stream.of(new HearingEventIgnored(hearingEvent.getHearingEventId(), hearingId, hearingEventDefinitionId, hearingEvent.getRecordedLabel(), hearingEvent.getEventTime(), ignoreReason.get(), alterable, hearingEvent.getNote()));
        }

        final String reference = getReference();

        if (isNull(reference)) {
            ignoreReason = Optional.of(REASON_CASEURN_NOT_FOUND);
            return Stream.of(new HearingEventIgnored(hearingEvent.getHearingEventId(), hearingId, hearingEventDefinitionId, hearingEvent.getRecordedLabel(), hearingEvent.getEventTime(), ignoreReason.get(), alterable, hearingEvent.getNote()));
        }


        final CourtCentre courtCentre = CourtCentre.courtCentre()
                .withId(momento.getHearing().getCourtCentre().getId())
                .withName(momento.getHearing().getCourtCentre().getName())
                .withRoomId(momento.getHearing().getCourtCentre().getRoomId())
                .withRoomName(momento.getHearing().getCourtCentre().getRoomName())
                .withWelshName(momento.getHearing().getCourtCentre().getWelshName())
                .withWelshRoomName(momento.getHearing().getCourtCentre().getWelshRoomName())
                .build();

        final HearingType hearingType = HearingType.hearingType()
                .withId(momento.getHearing().getType().getId())
                .withDescription(momento.getHearing().getType().getDescription())
                .build();

        return Stream.of(new HearingEventLogged(
                hearingEvent.getHearingEventId(),
                null,
                hearingId,
                hearingEventDefinitionId,
                defenceCounselId,
                hearingEvent.getRecordedLabel(),
                hearingEvent.getEventTime(),
                hearingEvent.getLastModifiedTime(),
                alterable,
                courtCentre,
                hearingType,
                reference,
                momento.getHearing().getJurisdictionType(),
                hearingEvent.getNote(),
                userId));
    }


    public Stream<Object> updateHearingEvents(final UUID hearingId, List<uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent> hearingEvents) {

        if (this.momento.getHearing() == null) {
            return Stream.of(raiseHearingEventIgnored(REASON_HEARING_NOT_FOUND, hearingId));
        }

        if (hearingEvents.isEmpty()) {
            return Stream.empty();
        }

        return Stream.of(new HearingEventsUpdated(hearingId, hearingEvents));
    }

    public Stream<Object> correctHearingEvent(final UUID latestHearingEventId, final UUID hearingId, final UUID hearingEventDefinitionId, final Boolean alterable, final UUID defenceCounselId, final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent, final UUID userId) {

        final Optional<String> ignoreReason = validateHearingEventBeforeApplyCorrection(hearingEvent.getHearingEventId());
        if (ignoreReason.isPresent()) {
            return Stream.of(new HearingEventIgnored(hearingEvent.getHearingEventId(), hearingId, hearingEventDefinitionId, hearingEvent.getRecordedLabel(), hearingEvent.getEventTime(), ignoreReason.get(), alterable, hearingEvent.getNote()));
        }

        return Stream.of(
                new HearingEventDeleted(hearingEvent.getHearingEventId(), userId),
                new HearingEventLogged(
                        latestHearingEventId,
                        hearingEvent.getHearingEventId(),
                        hearingId,
                        hearingEventDefinitionId,
                        defenceCounselId,
                        hearingEvent.getRecordedLabel(),
                        hearingEvent.getEventTime(),
                        hearingEvent.getLastModifiedTime(),
                        alterable,
                        CourtCentre.courtCentre()
                                .withId(momento.getHearing().getCourtCentre().getId())
                                .withName(momento.getHearing().getCourtCentre().getName())
                                .withRoomId(momento.getHearing().getCourtCentre().getRoomId())
                                .withRoomName(momento.getHearing().getCourtCentre().getRoomName())
                                .withWelshName(momento.getHearing().getCourtCentre().getWelshName())
                                .withWelshRoomName(momento.getHearing().getCourtCentre().getWelshRoomName())
                                .build(),
                        HearingType.hearingType()
                                .withId(momento.getHearing().getType().getId())
                                .withDescription(momento.getHearing().getType().getDescription())
                                .build(),
                        getReference(),
                        momento.getHearing().getJurisdictionType(),
                        hearingEvent.getNote(),
                        userId
                ));
    }

    private String getReference() {
        return ofNullable(this.momento.getHearing().getProsecutionCases())
                .map(c -> c.get(0).getProsecutionCaseIdentifier())
                .map(this::getProsecutionReference)
                .orElse(getReferenceFromCourtApplications());
    }

    private HearingEventIgnored raiseHearingEventIgnored(final String reason, final UUID hearingId) {
        return new HearingEventIgnored(
                hearingId,
                reason
        );
    }

    private Optional<String> validateHearingEvent(final UUID hearingEventId) {
        if (this.momento.getHearing() == null) {
            return Optional.of(REASON_HEARING_NOT_FOUND);
        }
        if (this.momento.getHearingEvents().containsKey(hearingEventId)) {
            if (this.momento.getHearingEvents().get(hearingEventId).isDeleted()) {
                return Optional.of(REASON_ALREADY_DELETED);
            }
            return Optional.of(REASON_ALREADY_LOGGED);
        }
        if(nonNull(momento.getHearing().getIsBoxHearing()) && momento.getHearing().getIsBoxHearing()) {
            return Optional.of(REASON_EVENT_LOG_NOT_ALLOWED_FOR_BOX_HEARING);
        }
        return Optional.empty();
    }

    private Optional<String> validateHearingEventBeforeApplyCorrection(final UUID hearingEventId) {
        if (this.momento.getHearing() == null) {
            return Optional.of(REASON_HEARING_NOT_FOUND);
        }
        if (!this.momento.getHearingEvents().containsKey(hearingEventId)) {

            return Optional.of(REASON_EVENT_NOT_FOUND);
        }
        if (this.momento.getHearingEvents().get(hearingEventId).isDeleted()) {
            return Optional.of(REASON_ALREADY_DELETED);
        }
        if(nonNull(momento.getHearing().getIsBoxHearing()) && momento.getHearing().getIsBoxHearing()) {
            return Optional.of(REASON_EVENT_LOG_NOT_ALLOWED_FOR_BOX_HEARING);
        }
        return Optional.empty();
    }

    public static final class HearingEvent implements Serializable {

        private static final long serialVersionUID = 1L;
        private final HearingEventLogged hearingEventLogged;
        private boolean deleted;

        public HearingEvent(final HearingEventLogged hearingEventLogged) {
            this.hearingEventLogged = hearingEventLogged;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(final boolean deleted) {
            this.deleted = deleted;
        }

        public HearingEventLogged getHearingEventLogged() {
            return hearingEventLogged;
        }
    }

    private String getReferenceFromCourtApplications() {
        return ofNullable(this.momento.getHearing().getCourtApplications())
                .map(courtApplications -> courtApplications.get(0))
                .map(CourtApplication::getApplicationReference).orElse(null);
    }

    private String getProsecutionReference(final ProsecutionCaseIdentifier prosecutionCaseIdentifier) {
        final String caseURN = prosecutionCaseIdentifier.getCaseURN();
        final String prosecutionAuthorityReference = prosecutionCaseIdentifier.getProsecutionAuthorityReference();
        return nonNull(caseURN) ? caseURN : prosecutionAuthorityReference;
    }
}
