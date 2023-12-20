package uk.gov.moj.cpp.hearing.domain.updatepleas;

import uk.gov.justice.core.courts.IndicatedPlea;

import java.util.List;
import java.util.UUID;

public class UpdateAssociatedHearingsWithIndicatedPlea {

    private IndicatedPlea indicatedPlea;

    private List<UUID> hearingIds;

    public IndicatedPlea getIndicatedPlea() {
        return indicatedPlea;
    }

    public void setIndicatedPlea(final IndicatedPlea indicatedPlea) {
        this.indicatedPlea = indicatedPlea;
    }

    @SuppressWarnings("squid:S2384")
    public List<UUID> getHearingIds() {
        return hearingIds;
    }
    @SuppressWarnings("squid:S2384")
    public void setHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
    }
}
