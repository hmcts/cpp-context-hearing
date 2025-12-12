package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.hearing-breach-Applications-to-be-added")
@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
public class HearingBreachApplicationsToBeAddedReceived implements Serializable {

    private List<UUID> courtApplications;

    public HearingBreachApplicationsToBeAddedReceived(final List<UUID> courtApplications) {
        this.courtApplications = courtApplications;
    }

    public List<UUID> getCourtApplications() {
        return courtApplications;
    }


}
