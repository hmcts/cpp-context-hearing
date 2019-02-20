package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.json.schemas.staging.Employer;

public class StagingEnforcementEmployerMapper {

    private final Defendant defendant;

    StagingEnforcementEmployerMapper(final Defendant defendant) {
        this.defendant = defendant;
    }

    public Employer map() {

        final PersonDefendant personDefendant = defendant.getPersonDefendant();

        final Organisation employerOrganisation = personDefendant.getEmployerOrganisation();

        if(isNull(employerOrganisation)) {
            return null;
        }

        final Address address = employerOrganisation.getAddress();

        final ContactNumber contact = employerOrganisation.getContact();

        final String emailAddress = nonNull(contact.getPrimaryEmail()) ? contact.getPrimaryEmail() : contact.getSecondaryEmail();

        return Employer.employer()
                .withEmployerReference(personDefendant.getEmployerPayrollReference())
                .withEmployerCompanyName(employerOrganisation.getName())
                .withEmployerAddress1(address.getAddress1())
                .withEmployerAddress2(address.getAddress2())
                .withEmployerAddress3(address.getAddress3())
                .withEmployerAddress4(address.getAddress4())
                .withEmployerAddress5(address.getAddress5())
                .withEmployerPostcode(address.getPostcode())
                .withEmployerTelephoneNumber(contact.getWork())
                .withEmployerEmailAddress(emailAddress)
                .build();
    }
}
