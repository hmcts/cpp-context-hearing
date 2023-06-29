package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.ProsecutionCase;
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
