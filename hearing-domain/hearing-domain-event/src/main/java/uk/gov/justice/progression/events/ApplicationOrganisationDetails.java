package uk.gov.justice.progression.events;

import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("public.progression.application-organisation-changed")
public class ApplicationOrganisationDetails {

    private UUID applicationId;

    private AssociatedDefenceOrganisation associatedDefenceOrganisation;

    private UUID subjectId;

    public static ApplicationOrganisationDetails applicationOrganisationDetails() { return new ApplicationOrganisationDetails(); }

    public UUID getApplicationId() {
        return applicationId;
    }

    public AssociatedDefenceOrganisation getAssociatedDefenceOrganisation() {
        return associatedDefenceOrganisation;
    }

    public ApplicationOrganisationDetails setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public ApplicationOrganisationDetails setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public ApplicationOrganisationDetails setAssociatedDefenceOrganisation(AssociatedDefenceOrganisation associatedDefenceOrganisation) {
        this.associatedDefenceOrganisation = associatedDefenceOrganisation;
        return this;
    }

    public UUID getSubjectId() {
        return subjectId;
    }
}