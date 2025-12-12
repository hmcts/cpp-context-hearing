package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

@Event("hearing.application-organisation-details-updated-for-hearing")
public class ApplicationOrganisationDetailsUpdatedForHearing implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID applicationId;

    private AssociatedDefenceOrganisation associatedDefenceOrganisation;

    private UUID subjectId;

    private UUID hearingId;

    @JsonCreator
    public ApplicationOrganisationDetailsUpdatedForHearing(
            final UUID applicationId,
            final UUID subjectId,
            final AssociatedDefenceOrganisation associatedDefenceOrganisation,
            final UUID hearingId) {

        this.applicationId = applicationId;
        this.subjectId = subjectId;
        this.associatedDefenceOrganisation = associatedDefenceOrganisation;
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public AssociatedDefenceOrganisation getAssociatedDefenceOrganisation() {
        return associatedDefenceOrganisation;
    }
}
