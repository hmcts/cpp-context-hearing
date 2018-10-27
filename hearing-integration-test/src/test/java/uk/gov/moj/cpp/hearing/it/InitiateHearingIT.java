package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.json.schemas.core.HearingLanguage.ENGLISH;
import static uk.gov.justice.json.schemas.core.JurisdictionType.MAGISTRATES;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForDefendantTypeOrganisation;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.json.schemas.core.Address;
import uk.gov.justice.json.schemas.core.AllocationDecision;
import uk.gov.justice.json.schemas.core.AssociatedPerson;
import uk.gov.justice.json.schemas.core.ContactNumber;
import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.IndicatedPlea;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import uk.gov.justice.json.schemas.core.LegalEntityDefendant;
import uk.gov.justice.json.schemas.core.NotifiedPlea;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.OffenceFacts;
import uk.gov.justice.json.schemas.core.Organisation;
import uk.gov.justice.json.schemas.core.Person;
import uk.gov.justice.json.schemas.core.PersonDefendant;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier;
import uk.gov.justice.json.schemas.core.ReferralReason;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseDefendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseHearing;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.ZoneId;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class InitiateHearingIT extends AbstractIT {

    @Test
    public void initiateHearing_withOnlyMandatoryFields() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .with(JudicialRole::getJudicialRoleType, is(judicialRole.getJudicialRoleType()))))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getInitiationCode, is(hearingOne.getFirstCase().getInitiationCode()))
                                .with(ProsecutionCase::getStatementOfFacts, is(hearingOne.getFirstCase().getStatementOfFacts()))
                                .with(ProsecutionCase::getStatementOfFactsWelsh, is(hearingOne.getFirstCase().getStatementOfFactsWelsh()))
                                .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getProsecutionAuthorityId()))
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getProsecutionAuthorityCode()))
                                        .with(ProsecutionCaseIdentifier::getCaseURN, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getCaseURN()))
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getProsecutionAuthorityReference())))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getProsecutionCaseId, is(hearingOne.getFirstCase().getId()))
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getOffenceDefinitionId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOffenceDefinitionId()))
                                                .with(Offence::getOffenceCode, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOffenceCode()))
                                                .with(Offence::getWording, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getWording()))
                                                .with(Offence::getStartDate, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getStartDate()))
                                                .with(Offence::getOrderIndex, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOrderIndex()))
                                                .with(Offence::getCount, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getCount()))
                                        ))
                                ))
                        ))
                )
        );

        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", 30,
                isBean(HearingListResponse.class)
                        .with(HearingListResponse::getHearings, first(isBean(HearingListResponseHearing.class)
                                .with(HearingListResponseHearing::getId, is(hearing.getId()))
                                .with(HearingListResponseHearing::getJurisdictionType, is(hearing.getJurisdictionType()))
                                .with(HearingListResponseHearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                                .with(HearingListResponseHearing::getHearingLanguage, is(HearingLanguage.ENGLISH.name()))
                                .with(HearingListResponseHearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getId, is(hearing.getType().getId()))
                                        .with(HearingType::getDescription, is(hearing.getType().getDescription())))
                                .with(HearingListResponseHearing::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                        .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))
                                        .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))))
                                .with(HearingListResponseHearing::getProsecutionCases, first(isBean(uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCase.class)
                                        .with(uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                        .with(uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                                .with(ProsecutionCaseIdentifier::getCaseURN, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getCaseURN()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getProsecutionAuthorityCode()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getProsecutionAuthorityId()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getProsecutionAuthorityReference())))
                                        .with(uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCase::getDefendants, first(isBean(HearingListResponseDefendant.class)
                                                .with(HearingListResponseDefendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                                .with(HearingListResponseDefendant::getName, is(hearingOne.getFirstDefendantForFirstCase().getPersonDefendant().getPersonDetails().getFirstName() + " " + hearingOne.getFirstDefendantForFirstCase().getPersonDefendant().getPersonDetails().getMiddleName() + " " + hearingOne.getFirstDefendantForFirstCase().getPersonDefendant().getPersonDetails().getLastName()))
                                        ))
                                ))
                        ))
        );
    }

    @Test
    public void initiateHearing_shouldInitiateHearing_whenDefendantTypeIsPerson() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);

        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = prosecutionCase.getProsecutionCaseIdentifier();

        final Defendant defendant = prosecutionCase.getDefendants().get(0);

        final Offence offence = defendant.getOffences().get(0);

        final HearingType hearingType = hearing.getType();

        final CourtCentre courtCentre = hearing.getCourtCentre();

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        final ReferralReason referralReason = hearing.getDefendantReferralReasons().get(0);

        final AssociatedPerson associatedPerson = defendant.getAssociatedPersons().get(0);

        final Organisation defenceOrganisation = defendant.getDefenceOrganisation();

        final Person person = associatedPerson.getPerson();

        final Address address = person.getAddress();

        final ContactNumber contact = person.getContact();

        final PersonDefendant personDefendant = defendant.getPersonDefendant();

        final Person personDetails = personDefendant.getPersonDetails();

        final Organisation employerOrganisation = personDefendant.getEmployerOrganisation();

        final Address employerAddress = employerOrganisation.getAddress();

        final ContactNumber employerContact = employerOrganisation.getContact();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getHasSharedResults, is(hearing.getHasSharedResults()))
                                .with(Hearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                                .with(Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getId, is(hearingType.getId()))
                                        .with(HearingType::getDescription, is(hearingType.getDescription())))
                                .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                                .with(Hearing::getHearingLanguage, is(ENGLISH))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(courtCentre.getId()))
                                        .with(CourtCentre::getName, is(courtCentre.getName()))
                                        .with(CourtCentre::getWelshName, is(courtCentre.getWelshName()))
                                        .with(CourtCentre::getRoomId, is(courtCentre.getRoomId()))
                                        .with(CourtCentre::getRoomName, is(courtCentre.getRoomName()))
                                        .with(CourtCentre::getWelshRoomName, is(courtCentre.getWelshRoomName())))
                                .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                        .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))
                                        .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))))
                                .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                        .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                        .with(JudicialRole::getTitle, is(judicialRole.getTitle()))
                                        .with(JudicialRole::getFirstName, is(judicialRole.getFirstName()))
                                        .with(JudicialRole::getMiddleName, is(judicialRole.getMiddleName()))
                                        .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                        .with(JudicialRole::getJudicialRoleType, is(judicialRole.getJudicialRoleType()))
                                        .with(JudicialRole::getIsDeputy, is(judicialRole.getIsDeputy()))
                                        .with(JudicialRole::getIsBenchChairman, is(judicialRole.getIsBenchChairman()))))
                                .with(Hearing::getDefendantReferralReasons, first(isBean(ReferralReason.class)
                                        .with(ReferralReason::getId, is(referralReason.getId()))
                                        .with(ReferralReason::getDescription, is(referralReason.getDescription()))
                                        .with(ReferralReason::getDefendantId, is(referralReason.getDefendantId()))))
                                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getId, is(prosecutionCase.getId()))
                                        .with(ProsecutionCase::getOriginatingOrganisation, is(prosecutionCase.getOriginatingOrganisation()))
                                        .with(ProsecutionCase::getCaseStatus, is(prosecutionCase.getCaseStatus()))
                                        .with(ProsecutionCase::getStatementOfFacts, is(prosecutionCase.getStatementOfFacts()))
                                        .with(ProsecutionCase::getStatementOfFactsWelsh, is(prosecutionCase.getStatementOfFactsWelsh()))
                                        .with(ProsecutionCase::getInitiationCode, is(prosecutionCase.getInitiationCode()))
                                        .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(prosecutionCaseIdentifier.getProsecutionAuthorityId()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(prosecutionCaseIdentifier.getProsecutionAuthorityCode()))
                                                .with(ProsecutionCaseIdentifier::getCaseURN, is(prosecutionCaseIdentifier.getCaseURN()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(prosecutionCaseIdentifier.getProsecutionAuthorityReference())))
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getId, is(defendant.getId()))
                                                .with(Defendant::getProsecutionCaseId, is(defendant.getProsecutionCaseId()))
                                                .with(Defendant::getNumberOfPreviousConvictionsCited, is(defendant.getNumberOfPreviousConvictionsCited()))
                                                .with(Defendant::getProsecutionAuthorityReference, is(defendant.getProsecutionAuthorityReference()))
                                                .with(Defendant::getWitnessStatement, is(defendant.getWitnessStatement()))
                                                .with(Defendant::getWitnessStatementWelsh, is(defendant.getWitnessStatementWelsh()))
                                                .with(Defendant::getMitigation, is(defendant.getMitigation()))
                                                .with(Defendant::getMitigationWelsh, is(defendant.getMitigationWelsh()))
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(offence.getId()))
                                                        .with(Offence::getOffenceDefinitionId, is(offence.getOffenceDefinitionId()))
                                                        .with(Offence::getOffenceCode, is(offence.getOffenceCode()))
                                                        .with(Offence::getWording, is(offence.getWording()))
                                                        .with(Offence::getStartDate, is(offence.getStartDate()))
                                                        .with(Offence::getOrderIndex, is(offence.getOrderIndex()))
                                                        .with(Offence::getCount, is(offence.getCount()))))
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
                                                                .with(Person::getEthnicity, is(person.getEthnicity()))
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
                                                                        .with(ContactNumber::getSecondaryEmail, is(contact.getSecondaryEmail()))))))
                                                .with(Defendant::getDefenceOrganisation, isBean(Organisation.class)
                                                        .with(Organisation::getId, is(defenceOrganisation.getId()))
                                                        .with(Organisation::getName, is(defenceOrganisation.getName()))
                                                        .with(Organisation::getIncorporationNumber, is(defenceOrganisation.getIncorporationNumber()))
                                                        .with(Organisation::getRegisteredCharityNumber, is(defenceOrganisation.getRegisteredCharityNumber()))
                                                        .with(Organisation::getAddress, isBean(Address.class)
                                                                .with(Address::getAddress1, is(defenceOrganisation.getAddress().getAddress1()))
                                                                .with(Address::getAddress2, is(defenceOrganisation.getAddress().getAddress2()))
                                                                .with(Address::getAddress3, is(defenceOrganisation.getAddress().getAddress3()))
                                                                .with(Address::getAddress4, is(defenceOrganisation.getAddress().getAddress4()))
                                                                .with(Address::getAddress5, is(defenceOrganisation.getAddress().getAddress5()))
                                                                .with(Address::getPostcode, is(defenceOrganisation.getAddress().getPostcode())))
                                                        .with(Organisation::getContact, isBean(ContactNumber.class)
                                                                .with(ContactNumber::getHome, is(defenceOrganisation.getContact().getHome()))
                                                                .with(ContactNumber::getWork, is(defenceOrganisation.getContact().getWork()))
                                                                .with(ContactNumber::getMobile, is(defenceOrganisation.getContact().getMobile()))
                                                                .with(ContactNumber::getPrimaryEmail, is(defenceOrganisation.getContact().getPrimaryEmail()))
                                                                .with(ContactNumber::getSecondaryEmail, is(defenceOrganisation.getContact().getSecondaryEmail()))))
                                                .with(Defendant::getPersonDefendant, isBean(PersonDefendant.class)
                                                        .with(PersonDefendant::getPersonDetails, isBean(Person.class)
                                                                .with(Person::getTitle, is(personDetails.getTitle()))
                                                                .with(Person::getFirstName, is(personDetails.getFirstName()))
                                                                .with(Person::getLastName, is(personDetails.getLastName()))
                                                                .with(Person::getMiddleName, is(personDetails.getMiddleName()))
                                                                .with(Person::getDateOfBirth, is(personDetails.getDateOfBirth()))
                                                                .with(Person::getNationalityId, is(personDetails.getNationalityId()))
                                                                .with(Person::getNationalityCode, is(personDetails.getNationalityCode()))
                                                                .with(Person::getAdditionalNationalityId, is(personDetails.getAdditionalNationalityId()))
                                                                .with(Person::getAdditionalNationalityCode, is(personDetails.getAdditionalNationalityCode()))
                                                                .with(Person::getDisabilityStatus, is(personDetails.getDisabilityStatus()))
                                                                .with(Person::getEthnicityId, is(personDetails.getEthnicityId()))
                                                                .with(Person::getEthnicity, is(personDetails.getEthnicity()))
                                                                .with(Person::getGender, is(personDetails.getGender()))
                                                                .with(Person::getInterpreterLanguageNeeds, is(personDetails.getInterpreterLanguageNeeds()))
                                                                .with(Person::getDocumentationLanguageNeeds, is(personDetails.getDocumentationLanguageNeeds()))
                                                                .with(Person::getNationalInsuranceNumber, is(personDetails.getNationalInsuranceNumber()))
                                                                .with(Person::getOccupation, is(personDetails.getOccupation()))
                                                                .with(Person::getOccupationCode, is(personDetails.getOccupationCode()))
                                                                .with(Person::getSpecificRequirements, is(personDetails.getSpecificRequirements()))
                                                                .with(Person::getAddress, isBean(Address.class)
                                                                        .with(Address::getAddress1, is(personDetails.getAddress().getAddress1()))
                                                                        .with(Address::getAddress2, is(personDetails.getAddress().getAddress2()))
                                                                        .with(Address::getAddress3, is(personDetails.getAddress().getAddress3()))
                                                                        .with(Address::getAddress4, is(personDetails.getAddress().getAddress4()))
                                                                        .with(Address::getAddress5, is(personDetails.getAddress().getAddress5()))
                                                                        .with(Address::getPostcode, is(personDetails.getAddress().getPostcode())))
                                                                .with(Person::getContact, isBean(ContactNumber.class)
                                                                        .with(ContactNumber::getHome, is(personDetails.getContact().getHome()))
                                                                        .with(ContactNumber::getWork, is(personDetails.getContact().getWork()))
                                                                        .with(ContactNumber::getMobile, is(personDetails.getContact().getMobile()))
                                                                        .with(ContactNumber::getPrimaryEmail, is(personDetails.getContact().getPrimaryEmail()))
                                                                        .with(ContactNumber::getSecondaryEmail, is(personDetails.getContact().getSecondaryEmail()))))
                                                        .with(PersonDefendant::getBailStatus, is(personDefendant.getBailStatus()))
                                                        .with(PersonDefendant::getCustodyTimeLimit, is(personDefendant.getCustodyTimeLimit()))
                                                        .with(PersonDefendant::getPerceivedBirthYear, is(personDefendant.getPerceivedBirthYear()))
                                                        .with(PersonDefendant::getObservedEthnicityId, is(personDefendant.getObservedEthnicityId()))
                                                        .with(PersonDefendant::getObservedEthnicityCode, is(personDefendant.getObservedEthnicityCode()))
                                                        .with(PersonDefendant::getSelfDefinedEthnicityId, is(personDefendant.getSelfDefinedEthnicityId()))
                                                        .with(PersonDefendant::getSelfDefinedEthnicityCode, is(personDefendant.getSelfDefinedEthnicityCode()))
                                                        .with(PersonDefendant::getDriverNumber, is(personDefendant.getDriverNumber()))
                                                        .with(PersonDefendant::getPncId, is(personDefendant.getPncId()))
                                                        .with(PersonDefendant::getArrestSummonsNumber, is(personDefendant.getArrestSummonsNumber()))
                                                        .with(PersonDefendant::getEmployerPayrollReference, is(personDefendant.getEmployerPayrollReference()))
                                                        .with(PersonDefendant::getEmployerOrganisation, isBean(Organisation.class)
                                                                .with(Organisation::getId, is(employerOrganisation.getId()))
                                                                .with(Organisation::getName, is(employerOrganisation.getName()))
                                                                .with(Organisation::getIncorporationNumber, is(employerOrganisation.getIncorporationNumber()))
                                                                .with(Organisation::getRegisteredCharityNumber, is(employerOrganisation.getRegisteredCharityNumber()))
                                                                .with(Organisation::getAddress, isBean(Address.class)
                                                                        .with(Address::getAddress1, is(employerAddress.getAddress1()))
                                                                        .with(Address::getAddress2, is(employerAddress.getAddress2()))
                                                                        .with(Address::getAddress3, is(employerAddress.getAddress3()))
                                                                        .with(Address::getAddress4, is(employerAddress.getAddress4()))
                                                                        .with(Address::getAddress5, is(employerAddress.getAddress5()))
                                                                        .with(Address::getPostcode, is(employerAddress.getPostcode())))
                                                                .with(Organisation::getContact, isBean(ContactNumber.class)
                                                                        .with(ContactNumber::getHome, is(employerContact.getHome()))
                                                                        .with(ContactNumber::getWork, is(employerContact.getWork()))
                                                                        .with(ContactNumber::getMobile, is(employerContact.getMobile()))
                                                                        .with(ContactNumber::getPrimaryEmail, is(employerContact.getPrimaryEmail()))
                                                                        .with(ContactNumber::getSecondaryEmail, is(employerContact.getSecondaryEmail())))))))))));
    }

    @Test
    public void initiateHearing_shouldInitiateHearing_whenDefendantTypeIsOrganisation() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingTemplateForDefendantTypeOrganisation()));

        final Hearing hearing = hearingOne.getHearing();

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);

        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = prosecutionCase.getProsecutionCaseIdentifier();

        final Defendant defendant = prosecutionCase.getDefendants().get(0);

        final Offence offence = defendant.getOffences().get(0);

        final HearingType hearingType = hearing.getType();

        final CourtCentre courtCentre = hearing.getCourtCentre();

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        final ReferralReason referralReason = hearing.getDefendantReferralReasons().get(0);

        final AssociatedPerson associatedPerson = defendant.getAssociatedPersons().get(0);

        final Organisation defenceOrganisation = defendant.getDefenceOrganisation();

        final Person person = associatedPerson.getPerson();

        final Address address = person.getAddress();

        final ContactNumber contact = person.getContact();

        final Organisation legalEntityDefendantOrganisation = defendant.getLegalEntityDefendant().getOrganisation();

        final IndicatedPlea indicatedPlea = offence.getIndicatedPlea();

        final AllocationDecision allocationDecision = indicatedPlea.getAllocationDecision();

        final NotifiedPlea notifiedPlea = offence.getNotifiedPlea();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getHasSharedResults, is(hearing.getHasSharedResults()))
                                .with(Hearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                                .with(Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getId, is(hearingType.getId()))
                                        .with(HearingType::getDescription, is(hearingType.getDescription())))
                                .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                                .with(Hearing::getHearingLanguage, is(ENGLISH))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(courtCentre.getId()))
                                        .with(CourtCentre::getName, is(courtCentre.getName()))
                                        .with(CourtCentre::getWelshName, is(courtCentre.getWelshName()))
                                        .with(CourtCentre::getRoomId, is(courtCentre.getRoomId()))
                                        .with(CourtCentre::getRoomName, is(courtCentre.getRoomName()))
                                        .with(CourtCentre::getWelshRoomName, is(courtCentre.getWelshRoomName())))
                                .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                        .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))
                                        .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))))
                                .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                        .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                        .with(JudicialRole::getTitle, is(judicialRole.getTitle()))
                                        .with(JudicialRole::getFirstName, is(judicialRole.getFirstName()))
                                        .with(JudicialRole::getMiddleName, is(judicialRole.getMiddleName()))
                                        .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                        .with(JudicialRole::getJudicialRoleType, is(judicialRole.getJudicialRoleType()))
                                        .with(JudicialRole::getIsDeputy, is(judicialRole.getIsDeputy()))
                                        .with(JudicialRole::getIsBenchChairman, is(judicialRole.getIsBenchChairman()))))
                                .with(Hearing::getDefendantReferralReasons, first(isBean(ReferralReason.class)
                                        .with(ReferralReason::getId, is(referralReason.getId()))
                                        .with(ReferralReason::getDescription, is(referralReason.getDescription()))
                                        .with(ReferralReason::getDefendantId, is(referralReason.getDefendantId()))))
                                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getId, is(prosecutionCase.getId()))
                                        .with(ProsecutionCase::getOriginatingOrganisation, is(prosecutionCase.getOriginatingOrganisation()))
                                        .with(ProsecutionCase::getCaseStatus, is(prosecutionCase.getCaseStatus()))
                                        .with(ProsecutionCase::getStatementOfFacts, is(prosecutionCase.getStatementOfFacts()))
                                        .with(ProsecutionCase::getStatementOfFactsWelsh, is(prosecutionCase.getStatementOfFactsWelsh()))
                                        .with(ProsecutionCase::getInitiationCode, is(prosecutionCase.getInitiationCode()))
                                        .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(prosecutionCaseIdentifier.getProsecutionAuthorityId()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(prosecutionCaseIdentifier.getProsecutionAuthorityCode()))
                                                .with(ProsecutionCaseIdentifier::getCaseURN, is(prosecutionCaseIdentifier.getCaseURN()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(prosecutionCaseIdentifier.getProsecutionAuthorityReference())))
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getId, is(defendant.getId()))
                                                .with(Defendant::getProsecutionCaseId, is(defendant.getProsecutionCaseId()))
                                                .with(Defendant::getNumberOfPreviousConvictionsCited, is(defendant.getNumberOfPreviousConvictionsCited()))
                                                .with(Defendant::getProsecutionAuthorityReference, is(defendant.getProsecutionAuthorityReference()))
                                                .with(Defendant::getWitnessStatement, is(defendant.getWitnessStatement()))
                                                .with(Defendant::getWitnessStatementWelsh, is(defendant.getWitnessStatementWelsh()))
                                                .with(Defendant::getMitigation, is(defendant.getMitigation()))
                                                .with(Defendant::getMitigationWelsh, is(defendant.getMitigationWelsh()))
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(offence.getId()))
                                                        .with(Offence::getOffenceDefinitionId, is(offence.getOffenceDefinitionId()))
                                                        .with(Offence::getOffenceCode, is(offence.getOffenceCode()))
                                                        .with(Offence::getOffenceTitle, is(offence.getOffenceTitle()))
                                                        .with(Offence::getOffenceTitleWelsh, is(offence.getOffenceTitleWelsh()))
                                                        .with(Offence::getOffenceLegislation, is(offence.getOffenceLegislation()))
                                                        .with(Offence::getOffenceLegislationWelsh, is(offence.getOffenceLegislationWelsh()))
                                                        .with(Offence::getModeOfTrial, is(offence.getModeOfTrial()))
                                                        .with(Offence::getWording, is(offence.getWording()))
                                                        .with(Offence::getWordingWelsh, is(offence.getWordingWelsh()))
                                                        .with(Offence::getStartDate, is(offence.getStartDate()))
                                                        .with(Offence::getEndDate, is(offence.getEndDate()))
                                                        .with(Offence::getArrestDate, is(offence.getArrestDate()))
                                                        .with(Offence::getChargeDate, is(offence.getChargeDate()))
                                                        .with(Offence::getOrderIndex, is(offence.getOrderIndex()))
                                                        .with(Offence::getCount, is(offence.getCount()))
                                                        .with(Offence::getConvictionDate, is(offence.getConvictionDate()))
                                                        .with(Offence::getNotifiedPlea, isBean(NotifiedPlea.class)
                                                                .with(NotifiedPlea::getOffenceId, is(notifiedPlea.getOffenceId()))
                                                                .with(NotifiedPlea::getNotifiedPleaDate, is(notifiedPlea.getNotifiedPleaDate()))
                                                                .with(NotifiedPlea::getNotifiedPleaValue, is(notifiedPlea.getNotifiedPleaValue())))
                                                        .with(Offence::getIndicatedPlea, isBean(IndicatedPlea.class)
                                                                .with(IndicatedPlea::getOffenceId, is(offence.getIndicatedPlea().getOffenceId()))
                                                                .with(IndicatedPlea::getIndicatedPleaDate, is(indicatedPlea.getIndicatedPleaDate()))
                                                                .with(IndicatedPlea::getIndicatedPleaValue, is(indicatedPlea.getIndicatedPleaValue()))
                                                                .with(IndicatedPlea::getSource, is(indicatedPlea.getSource()))
                                                                .with(IndicatedPlea::getAllocationDecision, isBean(AllocationDecision.class)
                                                                        .with(AllocationDecision::getCourtDecision, is(allocationDecision.getCourtDecision()))
                                                                        .with(AllocationDecision::getDefendantRepresentation, is(allocationDecision.getDefendantRepresentation()))
                                                                        .with(AllocationDecision::getProsecutionRepresentation, is(allocationDecision.getProsecutionRepresentation()))
                                                                        .with(AllocationDecision::getIndicationOfSentence, is(allocationDecision.getIndicationOfSentence()))))))
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
                                                                .with(Person::getEthnicity, is(person.getEthnicity()))
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
                                                                        .with(ContactNumber::getSecondaryEmail, is(contact.getSecondaryEmail()))))))
                                                .with(Defendant::getDefenceOrganisation, isBean(Organisation.class)
                                                        .with(Organisation::getId, is(defenceOrganisation.getId()))
                                                        .with(Organisation::getName, is(defenceOrganisation.getName()))
                                                        .with(Organisation::getIncorporationNumber, is(defenceOrganisation.getIncorporationNumber()))
                                                        .with(Organisation::getRegisteredCharityNumber, is(defenceOrganisation.getRegisteredCharityNumber()))
                                                        .with(Organisation::getAddress, isBean(Address.class)
                                                                .with(Address::getAddress1, is(defenceOrganisation.getAddress().getAddress1()))
                                                                .with(Address::getAddress2, is(defenceOrganisation.getAddress().getAddress2()))
                                                                .with(Address::getAddress3, is(defenceOrganisation.getAddress().getAddress3()))
                                                                .with(Address::getAddress4, is(defenceOrganisation.getAddress().getAddress4()))
                                                                .with(Address::getAddress5, is(defenceOrganisation.getAddress().getAddress5()))
                                                                .with(Address::getPostcode, is(defenceOrganisation.getAddress().getPostcode())))
                                                        .with(Organisation::getContact, isBean(ContactNumber.class)
                                                                .with(ContactNumber::getHome, is(defenceOrganisation.getContact().getHome()))
                                                                .with(ContactNumber::getWork, is(defenceOrganisation.getContact().getWork()))
                                                                .with(ContactNumber::getMobile, is(defenceOrganisation.getContact().getMobile()))
                                                                .with(ContactNumber::getPrimaryEmail, is(defenceOrganisation.getContact().getPrimaryEmail()))
                                                                .with(ContactNumber::getSecondaryEmail, is(defenceOrganisation.getContact().getSecondaryEmail()))))
                                                .with(Defendant::getLegalEntityDefendant, isBean(LegalEntityDefendant.class)
                                                        .with(LegalEntityDefendant::getOrganisation, isBean(Organisation.class)
                                                                .with(Organisation::getId, is(legalEntityDefendantOrganisation.getId()))
                                                                .with(Organisation::getName, is(legalEntityDefendantOrganisation.getName()))
                                                                .with(Organisation::getIncorporationNumber, is(legalEntityDefendantOrganisation.getIncorporationNumber()))
                                                                .with(Organisation::getRegisteredCharityNumber, is(legalEntityDefendantOrganisation.getRegisteredCharityNumber()))
                                                                .with(Organisation::getAddress, isBean(Address.class)
                                                                        .with(Address::getAddress1, is(legalEntityDefendantOrganisation.getAddress().getAddress1()))
                                                                        .with(Address::getAddress2, is(legalEntityDefendantOrganisation.getAddress().getAddress2()))
                                                                        .with(Address::getAddress3, is(legalEntityDefendantOrganisation.getAddress().getAddress3()))
                                                                        .with(Address::getAddress4, is(legalEntityDefendantOrganisation.getAddress().getAddress4()))
                                                                        .with(Address::getAddress5, is(legalEntityDefendantOrganisation.getAddress().getAddress5()))
                                                                        .with(Address::getPostcode, is(legalEntityDefendantOrganisation.getAddress().getPostcode())))
                                                                .with(Organisation::getContact, isBean(ContactNumber.class)
                                                                        .with(ContactNumber::getHome, is(legalEntityDefendantOrganisation.getContact().getHome()))
                                                                        .with(ContactNumber::getWork, is(legalEntityDefendantOrganisation.getContact().getWork()))
                                                                        .with(ContactNumber::getMobile, is(legalEntityDefendantOrganisation.getContact().getMobile()))
                                                                        .with(ContactNumber::getPrimaryEmail, is(legalEntityDefendantOrganisation.getContact().getPrimaryEmail()))
                                                                        .with(ContactNumber::getSecondaryEmail, is(legalEntityDefendantOrganisation.getContact().getSecondaryEmail())))))))))));
    }

    @Test
    public void initiateHearing_shouldInitiateHearing_whenJurisdictionTypeIsMagistrates() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingTemplateForMagistrates()));

        final Hearing hearing = hearingOne.getHearing();

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);

        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = prosecutionCase.getProsecutionCaseIdentifier();

        final Defendant defendant = prosecutionCase.getDefendants().get(0);

        final Offence offence = defendant.getOffences().get(0);

        final HearingType hearingType = hearing.getType();

        final CourtCentre courtCentre = hearing.getCourtCentre();

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        final ReferralReason referralReason = hearing.getDefendantReferralReasons().get(0);

        final IndicatedPlea indicatedPlea = offence.getIndicatedPlea();

        final AllocationDecision allocationDecision = indicatedPlea.getAllocationDecision();

        final NotifiedPlea notifiedPlea = offence.getNotifiedPlea();

        final OffenceFacts offenceFacts = offence.getOffenceFacts();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getHasSharedResults, is(hearing.getHasSharedResults()))
                                .with(Hearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                                .with(Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getId, is(hearingType.getId()))
                                        .with(HearingType::getDescription, is(hearingType.getDescription())))
                                .with(Hearing::getJurisdictionType, is(MAGISTRATES))
                                .with(Hearing::getHearingLanguage, is(ENGLISH))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(courtCentre.getId()))
                                        .with(CourtCentre::getName, is(courtCentre.getName()))
                                        .with(CourtCentre::getWelshName, is(courtCentre.getWelshName()))
                                        .with(CourtCentre::getRoomId, is(courtCentre.getRoomId()))
                                        .with(CourtCentre::getRoomName, is(courtCentre.getRoomName()))
                                        .with(CourtCentre::getWelshRoomName, is(courtCentre.getWelshRoomName())))
                                .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                        .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))
                                        .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))))
                                .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                        .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                        .with(JudicialRole::getTitle, is(judicialRole.getTitle()))
                                        .with(JudicialRole::getFirstName, is(judicialRole.getFirstName()))
                                        .with(JudicialRole::getMiddleName, is(judicialRole.getMiddleName()))
                                        .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                        .with(JudicialRole::getJudicialRoleType, is(judicialRole.getJudicialRoleType()))
                                        .with(JudicialRole::getIsDeputy, is(judicialRole.getIsDeputy()))
                                        .with(JudicialRole::getIsBenchChairman, is(judicialRole.getIsBenchChairman()))))
                                .with(Hearing::getDefendantReferralReasons, first(isBean(ReferralReason.class)
                                        .with(ReferralReason::getId, is(referralReason.getId()))
                                        .with(ReferralReason::getDescription, is(referralReason.getDescription()))
                                        .with(ReferralReason::getDefendantId, is(referralReason.getDefendantId()))))
                                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getId, is(prosecutionCase.getId()))
                                        .with(ProsecutionCase::getOriginatingOrganisation, is(prosecutionCase.getOriginatingOrganisation()))
                                        .with(ProsecutionCase::getCaseStatus, is(prosecutionCase.getCaseStatus()))
                                        .with(ProsecutionCase::getStatementOfFacts, is(prosecutionCase.getStatementOfFacts()))
                                        .with(ProsecutionCase::getStatementOfFactsWelsh, is(prosecutionCase.getStatementOfFactsWelsh()))
                                        .with(ProsecutionCase::getInitiationCode, is(prosecutionCase.getInitiationCode()))
                                        .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(prosecutionCaseIdentifier.getProsecutionAuthorityId()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(prosecutionCaseIdentifier.getProsecutionAuthorityCode()))
                                                .with(ProsecutionCaseIdentifier::getCaseURN, is(prosecutionCaseIdentifier.getCaseURN()))
                                                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(prosecutionCaseIdentifier.getProsecutionAuthorityReference())))
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getId, is(defendant.getId()))
                                                .with(Defendant::getProsecutionCaseId, is(defendant.getProsecutionCaseId()))
                                                .with(Defendant::getNumberOfPreviousConvictionsCited, is(defendant.getNumberOfPreviousConvictionsCited()))
                                                .with(Defendant::getProsecutionAuthorityReference, is(defendant.getProsecutionAuthorityReference()))
                                                .with(Defendant::getWitnessStatement, is(defendant.getWitnessStatement()))
                                                .with(Defendant::getWitnessStatementWelsh, is(defendant.getWitnessStatementWelsh()))
                                                .with(Defendant::getMitigation, is(defendant.getMitigation()))
                                                .with(Defendant::getMitigationWelsh, is(defendant.getMitigationWelsh()))
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(offence.getId()))
                                                        .with(Offence::getOffenceDefinitionId, is(offence.getOffenceDefinitionId()))
                                                        .with(Offence::getOffenceCode, is(offence.getOffenceCode()))
                                                        .with(Offence::getOffenceTitle, is(offence.getOffenceTitle()))
                                                        .with(Offence::getOffenceTitleWelsh, is(offence.getOffenceTitleWelsh()))
                                                        .with(Offence::getOffenceLegislation, is(offence.getOffenceLegislation()))
                                                        .with(Offence::getOffenceLegislationWelsh, is(offence.getOffenceLegislationWelsh()))
                                                        .with(Offence::getModeOfTrial, is(offence.getModeOfTrial()))
                                                        .with(Offence::getWording, is(offence.getWording()))
                                                        .with(Offence::getWordingWelsh, is(offence.getWordingWelsh()))
                                                        .with(Offence::getStartDate, is(offence.getStartDate()))
                                                        .with(Offence::getEndDate, is(offence.getEndDate()))
                                                        .with(Offence::getArrestDate, is(offence.getArrestDate()))
                                                        .with(Offence::getChargeDate, is(offence.getChargeDate()))
                                                        .with(Offence::getOrderIndex, is(offence.getOrderIndex()))
                                                        .with(Offence::getCount, is(offence.getCount()))
                                                        .with(Offence::getConvictionDate, is(offence.getConvictionDate()))
                                                        .with(Offence::getNotifiedPlea, isBean(NotifiedPlea.class)
                                                                .with(NotifiedPlea::getOffenceId, is(notifiedPlea.getOffenceId()))
                                                                .with(NotifiedPlea::getNotifiedPleaDate, is(notifiedPlea.getNotifiedPleaDate()))
                                                                .with(NotifiedPlea::getNotifiedPleaValue, is(notifiedPlea.getNotifiedPleaValue())))
                                                        .with(Offence::getIndicatedPlea, isBean(IndicatedPlea.class)
                                                                .with(IndicatedPlea::getOffenceId, is(offence.getIndicatedPlea().getOffenceId()))
                                                                .with(IndicatedPlea::getIndicatedPleaDate, is(indicatedPlea.getIndicatedPleaDate()))
                                                                .with(IndicatedPlea::getIndicatedPleaValue, is(indicatedPlea.getIndicatedPleaValue()))
                                                                .with(IndicatedPlea::getSource, is(indicatedPlea.getSource()))
                                                                .with(IndicatedPlea::getAllocationDecision, isBean(AllocationDecision.class)
                                                                        .with(AllocationDecision::getCourtDecision, is(allocationDecision.getCourtDecision()))
                                                                        .with(AllocationDecision::getDefendantRepresentation, is(allocationDecision.getDefendantRepresentation()))
                                                                        .with(AllocationDecision::getProsecutionRepresentation, is(allocationDecision.getProsecutionRepresentation()))
                                                                        .with(AllocationDecision::getIndicationOfSentence, is(allocationDecision.getIndicationOfSentence()))))
                                                        .with(Offence::getPlea, is(nullValue()))
                                                        .with(Offence::getOffenceFacts, isBean(OffenceFacts.class)
                                                                .with(OffenceFacts::getVehicleRegistration, is(offenceFacts.getVehicleRegistration()))
                                                                .with(OffenceFacts::getAlcoholReadingAmount, is(offenceFacts.getAlcoholReadingAmount()))
                                                                .with(OffenceFacts::getAlcoholReadingMethod, is(offenceFacts.getAlcoholReadingMethod())))))))))));
    }
}