package uk.gov.moj.cpp.hearing.eventlog;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings({"squid:S2384"})
public class HearingApplicationDetail implements Serializable {

    private UUID applicationId;
    private String applicationReference;
    private HearingDefendantDetail subject;


    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
    }

    public HearingDefendantDetail getSubject() {
        return subject;
    }

    public void setSubject(final HearingDefendantDetail subject) {
        this.subject = subject;
    }

    public String getApplicationReference() {
        return applicationReference;
    }

    public void setApplicationReference(final String applicationReference) {
        this.applicationReference = applicationReference;
    }
}
