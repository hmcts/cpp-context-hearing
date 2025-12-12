package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.event.hearing-days-without-court-centre-corrected")
public class HearingDaysWithoutCourtCentreCorrected implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private List<HearingDay> hearingDays;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public List<HearingDay> getHearingDays() {
        return hearingDays;
    }

    public void setHearingDays(final List<HearingDay> hearingDays) {
        this.hearingDays = hearingDays;
    }
}
