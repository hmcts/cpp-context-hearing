package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantDetailsChangedCommandTemplates.caseDefendantDetailsChangedCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import org.junit.Test;

public class CaseDefendantDetailsChangeIT extends AbstractIT {

    @Test
    public void updateCaseDefendantDetails_shouldUpdateDefendant_givenResultNotShared() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final CommandHelpers.CaseDefendantDetailsHelper defendantUpdates = h(UseCases.updateDefendants(
                with(caseDefendantDetailsChangedCommandTemplate(), template -> {
                            template.getDefendants().get(0).setId(hearingOne.getFirstDefendantForFirstCase().getId());
                            template.getDefendants().get(0).setProsecutionCaseId(hearingOne.getFirstDefendantForFirstCase().getProsecutionCaseId());
                        }
                )));

        final AssociatedPerson associatedPerson = defendantUpdates.getFirstDefendant().getAssociatedPersons().get(0);
        final Person person = associatedPerson.getPerson();
        final Address address = person.getAddress();
        final ContactNumber contact = person.getContact();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(defendantUpdates.getFirstDefendant().getId()))
                                        .with(Defendant::getProsecutionCaseId, is(defendantUpdates.getFirstDefendant().getProsecutionCaseId()))
                                        .with(Defendant::getNumberOfPreviousConvictionsCited, is(defendantUpdates.getFirstDefendant().getNumberOfPreviousConvictionsCited()))
                                        .with(Defendant::getProsecutionAuthorityReference, is(defendantUpdates.getFirstDefendant().getProsecutionAuthorityReference()))
                                        .with(Defendant::getWitnessStatement, is(defendantUpdates.getFirstDefendant().getWitnessStatement()))
                                        .with(Defendant::getWitnessStatementWelsh, is(defendantUpdates.getFirstDefendant().getWitnessStatementWelsh()))
                                        .with(Defendant::getMitigation, is(defendantUpdates.getFirstDefendant().getMitigation()))
                                        .with(Defendant::getMitigationWelsh, is(defendantUpdates.getFirstDefendant().getMitigationWelsh()))
                                        .with(Defendant::getPersonDefendant, isBean(PersonDefendant.class)

                                                .with(PersonDefendant::getArrestSummonsNumber, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getArrestSummonsNumber()))
                                                .with(PersonDefendant::getBailStatus, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getBailStatus()))
                                                .with(PersonDefendant::getCustodyTimeLimit, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getCustodyTimeLimit()))
                                                .with(PersonDefendant::getDriverNumber, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getDriverNumber()))
                                                .with(PersonDefendant::getEmployerPayrollReference, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getEmployerPayrollReference()))
                                                .with(PersonDefendant::getObservedEthnicityCode, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getObservedEthnicityCode()))
                                                .with(PersonDefendant::getObservedEthnicityId, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getObservedEthnicityId()))
                                                .with(PersonDefendant::getPerceivedBirthYear, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPerceivedBirthYear()))
                                                .with(PersonDefendant::getPncId, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPncId()))
                                                .with(PersonDefendant::getSelfDefinedEthnicityCode, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getSelfDefinedEthnicityCode()))
                                                .with(PersonDefendant::getSelfDefinedEthnicityId, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getSelfDefinedEthnicityId()))
                                                .with(PersonDefendant::getPersonDetails, isBean(Person.class)
                                                        .with(Person::getFirstName, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getFirstName()))
                                                        .with(Person::getLastName, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getLastName()))
                                                        .with(Person::getAddress, isBean(Address.class)
                                                                .with(Address::getAddress1, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress1()))
                                                                .with(Address::getAddress2, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress2()))
                                                                .with(Address::getAddress3, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress3()))
                                                                .with(Address::getAddress4, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress4()))
                                                                .with(Address::getPostcode, is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getPostcode()))
                                                        )))
                                        .with(Defendant::getAssociatedPersons, first(isBean(AssociatedPerson.class)
                                                .with(AssociatedPerson::getRole, is(associatedPerson.getRole()))
                                                .with(AssociatedPerson::getPerson, isBean(Person.class)
                                                        .with(Person::getTitle, is(person.getTitle()))
                                                        .with(Person::getFirstName, is(person.getFirstName()))
                                                        .with(Person::getLastName, is(person.getLastName()))
                                                        .with(Person::getMiddleName, is(person.getMiddleName()))
                                                        .with(Person::getDateOfBirth, is(person.getDateOfBirth()))
                                                        .with(Person::getNationalityId, is(person.getNationalityId()))
                                                        .with(Person::getNationalityCode, is(person.getNationalityCode()))
                                                        .with(Person::getAdditionalNationalityId, is(person.getAdditionalNationalityId()))
                                                        .with(Person::getAdditionalNationalityCode, is(person.getAdditionalNationalityCode()))
                                                        .with(Person::getDisabilityStatus, is(person.getDisabilityStatus()))
                                                        .with(Person::getEthnicityId, is(person.getEthnicityId()))
                                                        .with(Person::getEthnicityCode, is(person.getEthnicityCode()))
                                                        .with(Person::getGender, is(person.getGender()))
                                                        .with(Person::getInterpreterLanguageNeeds, is(person.getInterpreterLanguageNeeds()))
                                                        .with(Person::getDocumentationLanguageNeeds, is(person.getDocumentationLanguageNeeds()))
                                                        .with(Person::getNationalInsuranceNumber, is(person.getNationalInsuranceNumber()))
                                                        .with(Person::getOccupation, is(person.getOccupation()))
                                                        .with(Person::getOccupationCode, is(person.getOccupationCode()))
                                                        .with(Person::getSpecificRequirements, is(person.getSpecificRequirements()))
                                                        .with(Person::getAddress, isBean(Address.class)
                                                                .with(Address::getAddress1, is(address.getAddress1()))
                                                                .with(Address::getAddress2, is(address.getAddress2()))
                                                                .with(Address::getAddress3, is(address.getAddress3()))
                                                                .with(Address::getAddress4, is(address.getAddress4()))
                                                                .with(Address::getAddress5, is(address.getAddress5()))
                                                                .with(Address::getPostcode, is(address.getPostcode())))
                                                        .with(Person::getContact, isBean(ContactNumber.class)
                                                                .with(ContactNumber::getHome, is(contact.getHome()))
                                                                .with(ContactNumber::getWork, is(contact.getWork()))
                                                                .with(ContactNumber::getMobile, is(contact.getMobile()))
                                                                .with(ContactNumber::getPrimaryEmail, is(contact.getPrimaryEmail()))
                                                                .with(ContactNumber::getSecondaryEmail, is(contact.getSecondaryEmail())))
                                                )))
                                ))))));
    }
}