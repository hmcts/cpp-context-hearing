package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;

import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.json.schemas.staging.ParentGuardian;

import java.util.List;
import java.util.Optional;

public class StagingEnforcementParentGuardianMapper {

    private final Defendant defendant;

    StagingEnforcementParentGuardianMapper(final Defendant defendant) {
        this.defendant = defendant;
    }

    public ParentGuardian map() {

        return findParentOrGuardian(defendant.getAssociatedPersons())
                .map(AssociatedPerson::getPerson)
                .map(personDetails ->
                        ParentGuardian.parentGuardian()
                                .withName(personDetails.getFirstName() + " " + personDetails.getMiddleName() + " " + personDetails.getLastName())
                                .withAddress1(personDetails.getAddress().getAddress1())
                                .withAddress2(personDetails.getAddress().getAddress2())
                                .withAddress3(personDetails.getAddress().getAddress3())
                                .withAddress4(personDetails.getAddress().getAddress4())
                                .withAddress5(personDetails.getAddress().getAddress5())
                                .withPostcode(personDetails.getAddress().getPostcode())
                                .build()
                ).orElse(null);
    }

    private Optional<AssociatedPerson> findParentOrGuardian(final List<AssociatedPerson> associatedPersons) {
        final String parent = "parent";
        final String guardian = "guardian";
        if (isNull(associatedPersons) || associatedPersons.isEmpty()) {
            return Optional.empty();
        }
        final Optional<AssociatedPerson> parentAssociatedPerson = associatedPersons.stream()
                .filter(associatedPerson ->
                        associatedPerson.getPerson() != null && parent.equalsIgnoreCase(associatedPerson.getRole()))
                .findFirst();

        return Optional.ofNullable(
                parentAssociatedPerson.orElse(
                        associatedPersons.stream()
                                .filter(associatedPerson ->
                                        associatedPerson.getPerson() != null && guardian.equalsIgnoreCase(associatedPerson.getRole()))
                                .findFirst().orElse(null)
                ));
    }

}
