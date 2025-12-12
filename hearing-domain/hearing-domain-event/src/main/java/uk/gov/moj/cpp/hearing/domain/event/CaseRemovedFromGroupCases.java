package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.case-removed-from-group-cases")
public class CaseRemovedFromGroupCases implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final UUID groupId;
    private final ProsecutionCase removedCase;
    private final ProsecutionCase newGroupMaster;

    public CaseRemovedFromGroupCases(final UUID hearingId, final UUID groupId, final ProsecutionCase removedCase, final ProsecutionCase newGroupMaster) {
        this.hearingId = hearingId;
        this.groupId = groupId;
        this.removedCase = removedCase;
        this.newGroupMaster = newGroupMaster;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public ProsecutionCase getRemovedCase() {
        return removedCase;
    }

    public ProsecutionCase getNewGroupMaster() {
        return newGroupMaster;
    }
}