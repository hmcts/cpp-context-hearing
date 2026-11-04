package uk.gov.moj.cpp.hearing.dto;

import java.util.UUID;

/**
 * Lightweight projection of one (hearing, prosecution case) pair sitting on a given day, used by
 * hearing.get.hearing-cases-for-day to avoid hydrating the full Hearing entity graph.
 */
public class HearingCaseForDayRow {

    private final UUID hearingId;
    private final UUID courtCentreId;
    private final UUID courtRoomId;
    private final Boolean isGroupProceedings;
    private final UUID caseId;
    private final Boolean isGroupMaster;
    private final Boolean isGroupMember;

    public HearingCaseForDayRow(final UUID hearingId,
                                final UUID courtCentreId,
                                final UUID courtRoomId,
                                final Boolean isGroupProceedings,
                                final UUID caseId,
                                final Boolean isGroupMaster,
                                final Boolean isGroupMember) {
        this.hearingId = hearingId;
        this.courtCentreId = courtCentreId;
        this.courtRoomId = courtRoomId;
        this.isGroupProceedings = isGroupProceedings;
        this.caseId = caseId;
        this.isGroupMaster = isGroupMaster;
        this.isGroupMember = isGroupMember;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public Boolean getIsGroupProceedings() {
        return isGroupProceedings;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public Boolean getIsGroupMaster() {
        return isGroupMaster;
    }

    public Boolean getIsGroupMember() {
        return isGroupMember;
    }
}
