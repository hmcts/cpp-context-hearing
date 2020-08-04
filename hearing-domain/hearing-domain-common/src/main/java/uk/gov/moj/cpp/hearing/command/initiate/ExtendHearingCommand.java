package uk.gov.moj.cpp.hearing.command.initiate;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class ExtendHearingCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private CourtApplication courtApplication;
    private List<ProsecutionCase> prosecutionCases;
    private List<UUID> shadowListedOffences;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public CourtApplication getCourtApplication() {
        return courtApplication;
    }

    public void setCourtApplication(CourtApplication courtApplication) {
        this.courtApplication = courtApplication;
    }

    public List<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public void setProsecutionCases(final List<ProsecutionCase> prosecutionCases) {
        this.prosecutionCases = prosecutionCases;
    }

    public List<UUID> getShadowListedOffences() {
        return shadowListedOffences;
    }

    public void setShadowListedOffences(List<UUID> shadowListedOffences) {
        this.shadowListedOffences = shadowListedOffences;
    }
}
