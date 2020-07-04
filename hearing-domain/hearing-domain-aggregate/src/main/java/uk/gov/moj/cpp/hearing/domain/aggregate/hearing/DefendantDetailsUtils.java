package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;

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
        isSame = isChanged(isSame, Objects.equals(previousPerson.getDateOfBirth(), currentPerson.getDateOfBirth()));

        //Nationality
        isSame = isChanged(isSame, Objects.equals(previousPerson.getNationalityCode(), currentPerson.getNationalityCode()));

        //Address
        return isChanged(isSame, compareAddress(previousPerson.getAddress(), currentPerson.getAddress()));
    }


    private boolean compareAddress(final Address previousAddress, final Address currentAddress) {
        boolean isSame = isChanged(true, Objects.equals(previousAddress.getAddress1(), currentAddress.getAddress1()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getAddress2(), currentAddress.getAddress2()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getAddress3(), currentAddress.getAddress3()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getAddress4(), currentAddress.getAddress4()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getAddress5(), currentAddress.getAddress5()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getWelshAddress1(), currentAddress.getWelshAddress1()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getWelshAddress1(), currentAddress.getWelshAddress1()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getWelshAddress2(), currentAddress.getWelshAddress2()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getWelshAddress3(), currentAddress.getWelshAddress3()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getWelshAddress4(), currentAddress.getWelshAddress4()));
        isSame = isChanged(isSame, Objects.equals(previousAddress.getWelshAddress5(), currentAddress.getWelshAddress5()));
        return isChanged(isSame, Objects.equals(previousAddress.getPostcode(), currentAddress.getPostcode()));
    }

    private boolean isChanged(final boolean changed, final boolean isChanged) {
        return changed && isChanged;
    }
}
