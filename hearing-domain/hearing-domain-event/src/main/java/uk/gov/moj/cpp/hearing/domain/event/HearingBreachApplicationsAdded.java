package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;

@Event("hearing.events.hearing-breach-applications-added")
@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
public class HearingBreachApplicationsAdded implements Serializable {

    private final  List<CourtApplication> courtApplications;

    public HearingBreachApplicationsAdded(final List<CourtApplication> courtApplications) {
        this.courtApplications = courtApplications;
    }

    public List<CourtApplication> getCourtApplications() {
        return courtApplications;
    }
}
