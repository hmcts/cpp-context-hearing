package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;

import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.json.schemas.staging.ParentGuardian;

import java.util.List;

public class StagingEnforcementParentGuardianMapper {

    private final Defendant defendant;

    StagingEnforcementParentGuardianMapper(final Defendant defendant) {
        this.defendant = defendant;
    }

    public ParentGuardian map() {
        if (isParentOrGuardianRoleSet(defendant.getAssociatedPersons())) {

            final Person personDetails = defendant.getPersonDefendant().getPersonDetails();

            return ParentGuardian.parentGuardian()
                    .withName(personDetails.getFirstName() + " " + personDetails.getMiddleName() + " " + personDetails.getLastName())
                    .withAddress1(personDetails.getAddress().getAddress1())
                    .withAddress2(personDetails.getAddress().getAddress2())
                    .withAddress3(personDetails.getAddress().getAddress3())
                    .withAddress4(personDetails.getAddress().getAddress4())
                    .withAddress5(personDetails.getAddress().getAddress5())
                    .withPostcode(personDetails.getAddress().getPostcode())
                    .build();
        }

        return null;
    }

    private boolean isParentOrGuardianRoleSet(final List<AssociatedPerson> associatedPersons) {

        final String parent = "parent";
        final String guardian = "guardian";

        if (isNull(associatedPersons) || associatedPersons.isEmpty()) {
            return false;
        }

        return associatedPersons.stream()
                .filter(associatedPerson -> associatedPerson.getRole().equalsIgnoreCase(parent))
                .anyMatch(associatedPerson -> associatedPerson.getRole().equalsIgnoreCase(guardian));
    }
}
