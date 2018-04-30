package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.List;
import java.util.UUID;

public class ProsecutionAdvocate extends Attendee<ProsecutionAdvocate> {

    private List<UUID> caseIds;

    private String status;

    public List<UUID> getCaseIds() {
        return caseIds;
    }

    public String getStatus() {
        return status;
    }

    public ProsecutionAdvocate setCaseIds(List<UUID> caseIds) {
        this.caseIds = caseIds;
        return this;
    }

    public ProsecutionAdvocate setStatus(String status) {
        this.status = status;
        return this;
    }

    public static ProsecutionAdvocate prosecutionAdvocate() {
        return new ProsecutionAdvocate();
    }
}
