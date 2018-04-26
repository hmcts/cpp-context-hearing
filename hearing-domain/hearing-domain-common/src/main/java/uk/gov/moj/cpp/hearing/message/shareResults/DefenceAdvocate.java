package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.List;
import java.util.UUID;

public class DefenceAdvocate extends Attendee<DefenceAdvocate> {

    private List<UUID> defendantIds;

    private String status;

    public List<UUID> getDefendantIds() {
        return defendantIds;
    }

    public String getStatus() {
        return status;
    }

    public DefenceAdvocate setDefendantIds(List<UUID> defendantIds) {
        this.defendantIds = defendantIds;
        return this;
    }

    public DefenceAdvocate setStatus(String status) {
        this.status = status;
        return this;
    }

    public static DefenceAdvocate defenceAdvocate() {
        return new DefenceAdvocate();
    }
}
