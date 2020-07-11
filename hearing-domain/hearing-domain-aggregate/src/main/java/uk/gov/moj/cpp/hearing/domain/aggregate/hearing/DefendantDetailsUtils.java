package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;

import java.time.LocalDate;
import java.util.Objects;

public class DefendantDetailsUtils {

    public boolean verifyDDCHOnRequiredAttributes(final Defendant previousDefendant, final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendant) {
        final PersonDefendant previousDefendantDetails = previousDefendant.getPersonDefendant();
        if (nonNull(previousDefendantDetails)) {
            return verifyDDCHOnRequiredAttributesForIndividual(previousDefendantDetails, updatedDefendant);
        } else {
            return verifyDDCHOnRequiredAttributesForOrganisation(previousDefendant, updatedDefendant);
        }
    }

    private boolean verifyDDCHOnRequiredAttributesForOrganisation(final Defendant previousDefendant, final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendant) {
        final LegalEntityDefendant previousOrgDefendantDetails = previousDefendant.getLegalEntityDefendant();
        final LegalEntityDefendant updatedOrgDefendantDetails = updatedDefendant.getLegalEntityDefendant();
        final boolean isSame = Objects.equals(previousOrgDefendantDetails.getOrganisation().getName(), updatedOrgDefendantDetails.getOrganisation().getName());
        return isChanged(isSame, compareAddress(previousOrgDefendantDetails.getOrganisation().getAddress(), updatedOrgDefendantDetails.getOrganisation().getAddress()));
    }

    private boolean verifyDDCHOnRequiredAttributesForIndividual(final PersonDefendant previousDefendant, final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendant) {
        final Person currentPerson = updatedDefendant.getPersonDefendant().getPersonDetails();
        final Person previousPerson = previousDefendant.getPersonDetails();

        //Name Check
        boolean isSame = isChanged(true, Objects.equals(previousPerson.getLastName(), currentPerson.getLastName()));
        isSame = isChanged(isSame, Objects.equals(previousPerson.getFirstName(), currentPerson.getFirstName()));
        isSame = isChanged(isSame, Objects.equals(previousPerson.getMiddleName(), currentPerson.getMiddleName()));

        //Date of Birth
        isSame = isChanged(isSame, compareLocalDate(previousPerson.getDateOfBirth(), currentPerson.getDateOfBirth()));

        //Nationality
        isSame = isChanged(isSame, Objects.equals(previousPerson.getNationalityCode(), currentPerson.getNationalityCode()));

        //Address
        return isChanged(isSame, compareAddress(previousPerson.getAddress(), currentPerson.getAddress()));
    }

    /**
     * Compare to Local Dates. Uses {@link LocalDate#MIN} to avoid null checking.
     *
     * @param previousLocalDate
     * @param currentLocalDate
     * @return TRUE if the two dates are the same, false otherwise
     */
    private boolean compareLocalDate(final LocalDate previousLocalDate, final LocalDate currentLocalDate) {
        final LocalDate a = previousLocalDate == null ? LocalDate.MIN : previousLocalDate;
        final LocalDate b = currentLocalDate == null ? LocalDate.MIN : currentLocalDate;

        return a.compareTo(b) == 0;
    }

    private boolean compareAddress(final Address previousAddress, final Address currentAddress) {
        return Objects.equals(previousAddress, currentAddress);
    }

    private boolean isChanged(final boolean changed, final boolean isChanged) {
        return changed && isChanged;
    }
}
