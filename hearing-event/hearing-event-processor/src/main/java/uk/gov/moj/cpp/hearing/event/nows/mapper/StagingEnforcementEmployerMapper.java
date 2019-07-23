package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.json.schemas.staging.Employer;

public class StagingEnforcementEmployerMapper {

    private final Organisation employer;

    StagingEnforcementEmployerMapper(final Organisation employer) {
        this.employer = employer;
    }

    public Employer map() {

        if (isNull(employer)) {
            return null;
        }

        final Address address = employer.getAddress();

        final ContactNumber contact = employer.getContact();

        String emailAddress = null;

        String work = null;

        if (nonNull(contact)) {
            emailAddress = nonNull(contact.getPrimaryEmail()) ? contact.getPrimaryEmail() : contact.getSecondaryEmail();
            work = contact.getWork();
        }

        return Employer.employer()
                .withEmployerCompanyName(employer.getName())
                .withEmployerAddress1(address.getAddress1())
                .withEmployerAddress2(address.getAddress2())
                .withEmployerAddress3(address.getAddress3())
                .withEmployerAddress4(address.getAddress4())
                .withEmployerAddress5(address.getAddress5())
                .withEmployerPostcode(address.getPostcode())
                .withEmployerTelephoneNumber(work)
                .withEmployerEmailAddress(emailAddress)
                .build();
    }
}
