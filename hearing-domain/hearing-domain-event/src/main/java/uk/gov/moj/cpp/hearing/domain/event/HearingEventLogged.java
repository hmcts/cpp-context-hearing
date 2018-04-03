package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.hearing-event-logged")
public class HearingEventLogged {

    private UUID hearingEventId;
    private UUID lastHearingEventId;
    private UUID hearingId;
    private UUID hearingEventDefinitionId;
    private String recordedLabel;
    private ZonedDateTime eventTime;
    private ZonedDateTime lastModifiedTime;
    private boolean alterable;

    private UUID courtCentreId;
    private String courtCentreName;
    private UUID courtRoomId;
    private String courtRoomName;

    private String hearingType;

    private String caseUrn;
    private UUID caseId;

    public HearingEventLogged(
            UUID hearingEventId,
            UUID lastHearingEventId,
            UUID hearingId,
            UUID hearingEventDefinitionId,
            String recordedLabel,
            ZonedDateTime eventTime,
            ZonedDateTime lastModifiedTime,
            boolean alterable,
            UUID courtCentreId,
            String courtCentreName,
            UUID courtRoomId,
            String courtRoomName,
            String hearingType,
            String caseUrn,
            UUID caseId) {

        this.hearingEventId = hearingEventId;
        this.lastHearingEventId = lastHearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
        this.courtRoomId = courtRoomId;
        this.courtRoomName = courtRoomName;
        this.hearingType = hearingType;
        this.caseUrn = caseUrn;
        this.caseId = caseId;
    }

    public HearingEventLogged() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getLastHearingEventId() {
        return lastHearingEventId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public ZonedDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public String getHearingType() {
        return hearingType;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public UUID getCaseId() {
        return caseId;
    }
}
