package uk.gov.moj.cpp.hearing.domain.updatepleas;

import uk.gov.justice.core.courts.Plea;

import java.util.List;
import java.util.UUID;

public class UpdateInheritedPleaCommand {

    private Plea plea;

    private List<UUID> hearingIds;

    public Plea getPlea() {
        return plea;
    }

    public void setPlea(Plea plea) {
        this.plea = plea;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public void setHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
    }
}
