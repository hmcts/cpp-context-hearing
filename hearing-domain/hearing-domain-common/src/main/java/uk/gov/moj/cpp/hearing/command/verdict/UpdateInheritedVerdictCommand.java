package uk.gov.moj.cpp.hearing.command.verdict;

import uk.gov.justice.core.courts.Verdict;

import java.util.List;
import java.util.UUID;

public class UpdateInheritedVerdictCommand {

    private Verdict verdict;

    private List<UUID> hearingIds;

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public void setHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
    }
}
