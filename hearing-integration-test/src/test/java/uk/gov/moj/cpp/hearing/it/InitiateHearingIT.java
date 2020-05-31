package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForCrownCourtOffenceCountNull;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForDefendantTypeOrganisation;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtIndicatedSentence;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Ethnicity;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.NotifiedPlea;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ReferralReason;
import uk.gov.justice.hearing.courts.CourtApplicationSummaries;
import uk.gov.justice.hearing.courts.Defendants;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.hearing.courts.ProsecutionCaseSummaries;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matcher;
import org.junit.Test;

public class InitiateHearingIT extends AbstractIT {

    private static final String COURT_ROOM_NAME = "Room 1";

    @Test
    public void initiateHearing_withOnlyMandatoryFields() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearing.getCourtCentre().getName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, courtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, courtApplication.getApplicationReference())
                        ))
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
                                        .with(Defendant::getMasterDefendantId, is(hearingOne.getFirstDefendantForFirstCase().getMasterDefendantId()))
                                        .with(Defendant::getCourtProceedingsInitiated, is(hearingOne.getFirstDefendantForFirstCase().getCourtProceedingsInitiated().withZoneSameLocal(ZoneId.of("UTC"))))
                                        .with(Defendant::getProsecutionCaseId, is(hearingOne.getFirstCase().getId()))
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getOffenceDefinitionId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOffenceDefinitionId()))
                                                .with(Offence::getOffenceCode, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOffenceCode()))
                                                .with(Offence::getWording, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getWording()))
                                                .with(Offence::getStartDate, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getStartDate()))
                                                .with(Offence::getOrderIndex, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOrderIndex()))
                                                .with(Offence::getCount, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getCount()))
                                                .with(Offence::getLaidDate, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getLaidDate()))
                                        ))
                                ))
                        ))
                )
        );
//TODO court applications
        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                //.withValue(HearingSummaries::getJurisdictionType, hearing.getJurisdictionType())
                                .withValue(HearingSummaries::getReportingRestrictionReason, hearing.getReportingRestrictionReason())
                                .withValue(HearingSummaries::getHearingLanguage, ENGLISH.name())
                                .with(HearingSummaries::getCourtCentre, isBean(CourtCentre.class)
                                        .withValue(CourtCentre::getId, hearing.getCourtCentre().getId())
                                        .withValue(CourtCentre::getName, hearing.getCourtCentre().getName()))
                                .with(HearingSummaries::getType, isBean(HearingType.class)
                                        .withValue(HearingType::getId, hearing.getType().getId())
                                        .withValue(HearingType::getDescription, hearing.getType().getDescription()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .withValue(HearingDay::getSittingDay, hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC")))
                                        .withValue(HearingDay::getListedDurationMinutes, hearingDay.getListedDurationMinutes())
                                        .withValue(HearingDay::getListingSequence, hearingDay.getListingSequence())))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasProsecutionSummaries(hearing.getProsecutionCases()))
                                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                                        .withValue(CourtApplicationSummaries::getId, courtApplication.getId())
                                ))
                        ))
        );
    }

    @Test
    public void initiateHearing_ApplicationOnly() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        hearing.setProsecutionCases(null);
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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
                                .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, courtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, courtApplication.getApplicationReference())
                        ))

                )
        );
//TODO court applications
        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                //.withValue(HearingSummaries::getJurisdictionType, hearing.getJurisdictionType())
                                .withValue(HearingSummaries::getReportingRestrictionReason, hearing.getReportingRestrictionReason())
                                .withValue(HearingSummaries::getHearingLanguage, HearingLanguage.ENGLISH.name())
                                .with(HearingSummaries::getType, isBean(HearingType.class)
                                        .withValue(HearingType::getId, hearing.getType().getId())
                                        .withValue(HearingType::getDescription, hearing.getType().getDescription()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .withValue(HearingDay::getSittingDay, hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC")))
                                        .withValue(HearingDay::getListedDurationMinutes, hearingDay.getListedDurationMinutes())
                                        .withValue(HearingDay::getListingSequence, hearingDay.getListingSequence())))
                                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                                        .withValue(CourtApplicationSummaries::getId, courtApplication.getId())
                                ))
                        ))
        );
    }


    @Test
    public void initiateHearing_shouldInitiateHearing_whenDefendantTypeIsPerson() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

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

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getHasSharedResults, is(false))
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
                                        .withValue(jr -> jr.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())
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
                                        .with(ProsecutionCase::getCaseMarkers, first(isBean(Marker.class)
                                                .with(Marker::getId, is(hearingOne.getFirstCase().getCaseMarkers().get(0).getId()))))
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getId, is(defendant.getId()))
                                                .with(Defendant::getMasterDefendantId, is(defendant.getMasterDefendantId()))
                                                .with(Defendant::getCourtProceedingsInitiated, is(defendant.getCourtProceedingsInitiated().withZoneSameLocal(ZoneId.of("UTC"))))
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
                                                        .with(Offence::getCount, is(offence.getCount()))
                                                        .with(Offence::getCustodyTimeLimit, isBean(CustodyTimeLimit.class)
                                                                .withValue(CustodyTimeLimit::getDaysSpent, offence.getCustodyTimeLimit().getDaysSpent())
                                                                .withValue(CustodyTimeLimit::getTimeLimit, offence.getCustodyTimeLimit().getTimeLimit())
                                                        )
                                                ))
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
                                                                .with(Person::getNationalityDescription, is(person.getNationalityDescription()))
                                                                .with(Person::getAdditionalNationalityId, is(person.getAdditionalNationalityId()))
                                                                .with(Person::getAdditionalNationalityCode, is(person.getAdditionalNationalityCode()))
                                                                .with(Person::getDisabilityStatus, is(person.getDisabilityStatus()))
//                                                                .with(Person::getEthnicityId, is(person.getEthnicityId()))
//                                                                .with(Person::getEthnicityCode, is(person.getEthnicityCode()))
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
//                                                                .with(Person::getEthnicityId, is(personDetails.getEthnicityId()))
//                                                                .with(Person::getEthnicityCode, is(personDetails.getEthnicityCode()))
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
//                                                        .with(PersonDefendant::getObservedEthnicityId, is(personDefendant.getObservedEthnicityId()))
//                                                        .with(PersonDefendant::getObservedEthnicityCode, is(personDefendant.getObservedEthnicityCode()))
//                                                        .with(PersonDefendant::getSelfDefinedEthnicityId, is(personDefendant.getSelfDefinedEthnicityId()))
//                                                        .with(PersonDefendant::getSelfDefinedEthnicityCode, is(personDefendant.getSelfDefinedEthnicityCode()))
                                                        .with(PersonDefendant::getDriverNumber, is(personDefendant.getDriverNumber()))
//                                                        .with(PersonDefendant::getPncId, is(personDefendant.getPncId()))
                                                        .with(PersonDefendant::getArrestSummonsNumber, is(personDefendant.getArrestSummonsNumber()))
                                                        .with(PersonDefendant::getEmployerPayrollReference, is(personDefendant.getEmployerPayrollReference()))
                                                        .with(PersonDefendant::getEmployerOrganisation, isBean(Organisation.class)
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

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateForDefendantTypeOrganisation()));

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

        final uk.gov.justice.core.courts.Ethnicity associatedPersonEthnicity = person.getEthnicity();

        final ContactNumber contact = person.getContact();

        final Organisation legalEntityDefendantOrganisation = defendant.getLegalEntityDefendant().getOrganisation();

        final IndicatedPlea indicatedPlea = offence.getIndicatedPlea();

        final AllocationDecision allocationDecision = offence.getAllocationDecision();

        final CourtIndicatedSentence courtIndicatedSentence = allocationDecision.getCourtIndicatedSentence();

        final NotifiedPlea notifiedPlea = offence.getNotifiedPlea();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getHasSharedResults, is(false))
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
                                        .withValue(jr -> jr.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())
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
                                                .with(Defendant::getMasterDefendantId, is(defendant.getMasterDefendantId()))
                                                .with(Defendant::getCourtProceedingsInitiated, is(defendant.getCourtProceedingsInitiated().withZoneSameLocal(ZoneId.of("UTC"))))
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
                                                                .with(IndicatedPlea::getSource, is(indicatedPlea.getSource())))
                                                        .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                                .with(AllocationDecision::getOriginatingHearingId, is(allocationDecision.getOriginatingHearingId()))
                                                                .with(AllocationDecision::getMotReasonId, is(allocationDecision.getMotReasonId()))
                                                                .with(AllocationDecision::getMotReasonDescription, is(allocationDecision.getMotReasonDescription()))
                                                                .with(AllocationDecision::getMotReasonCode, is(allocationDecision.getMotReasonCode()))
                                                                .with(AllocationDecision::getSequenceNumber, is(allocationDecision.getSequenceNumber()))
                                                                .with(AllocationDecision::getAllocationDecisionDate, is(allocationDecision.getAllocationDecisionDate()))
                                                                .with(AllocationDecision::getCourtIndicatedSentence, isBean(CourtIndicatedSentence.class)
                                                                        .with(CourtIndicatedSentence::getCourtIndicatedSentenceTypeId, is(courtIndicatedSentence.getCourtIndicatedSentenceTypeId()))
                                                                        .with(CourtIndicatedSentence::getCourtIndicatedSentenceDescription, is(courtIndicatedSentence.getCourtIndicatedSentenceDescription()))))))
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
                                                                .with(Person::getEthnicity, isBean(Ethnicity.class)
                                                                        .with(Ethnicity::getObservedEthnicityCode, is(associatedPersonEthnicity.getObservedEthnicityCode())))
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
                                                                        .with(ContactNumber::getSecondaryEmail, is(legalEntityDefendantOrganisation.getContact().getSecondaryEmail())))))))))))
        ;
    }

    @Test
    public void initiateHearing_shouldInitiateHearing_whenJurisdictionTypeIsMagistrates() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateForMagistrates()));

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

        final AllocationDecision allocationDecision = offence.getAllocationDecision();

        final CourtIndicatedSentence courtIndicatedSentence = allocationDecision.getCourtIndicatedSentence();

        final NotifiedPlea notifiedPlea = offence.getNotifiedPlea();

        final OffenceFacts offenceFacts = offence.getOffenceFacts();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getHasSharedResults, is(false))
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
                                        .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())
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
                                                .with(Defendant::getMasterDefendantId, is(defendant.getMasterDefendantId()))
                                                .with(Defendant::getCourtProceedingsInitiated, is(defendant.getCourtProceedingsInitiated().withZoneSameLocal(ZoneId.of("UTC"))))
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
                                                                .with(IndicatedPlea::getSource, is(indicatedPlea.getSource())))
                                                        .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                                .with(AllocationDecision::getOriginatingHearingId, is(allocationDecision.getOriginatingHearingId()))
                                                                .with(AllocationDecision::getMotReasonId, is(allocationDecision.getMotReasonId()))
                                                                .with(AllocationDecision::getMotReasonDescription, is(allocationDecision.getMotReasonDescription()))
                                                                .with(AllocationDecision::getMotReasonCode, is(allocationDecision.getMotReasonCode()))
                                                                .with(AllocationDecision::getSequenceNumber, is(allocationDecision.getSequenceNumber()))
                                                                .with(AllocationDecision::getAllocationDecisionDate, is(allocationDecision.getAllocationDecisionDate()))
                                                                .with(AllocationDecision::getCourtIndicatedSentence, isBean(CourtIndicatedSentence.class)
                                                                        .with(CourtIndicatedSentence::getCourtIndicatedSentenceTypeId, is(courtIndicatedSentence.getCourtIndicatedSentenceTypeId()))
                                                                        .with(CourtIndicatedSentence::getCourtIndicatedSentenceDescription, is(courtIndicatedSentence.getCourtIndicatedSentenceDescription()))))
                                                        .with(Offence::getPlea, is(nullValue()))
                                                        .with(Offence::getOffenceFacts, isBean(OffenceFacts.class)
                                                                .with(OffenceFacts::getVehicleRegistration, is(offenceFacts.getVehicleRegistration()))
                                                                .with(OffenceFacts::getAlcoholReadingAmount, is(offenceFacts.getAlcoholReadingAmount()))
                                                                .with(OffenceFacts::getAlcoholReadingMethodCode, is(offenceFacts.getAlcoholReadingMethodCode())))))))))));
    }

    @Test
    public void ignoreInitiateHearing_whenOffenceCountMissingForCrownCourtHearing() {

        UseCases.verifyIgnoreInitiateHearing(getRequestSpec(), initiateHearingTemplateForCrownCourtOffenceCountNull());

    }

    @Test
    public void listingHearings_with_sorted_listingSequence() throws NoSuchAlgorithmException {

        UUID courtAndRoomId = UUID.randomUUID();

        final LocalDate fifthJuly = LocalDate.of(2019, 7, 5);
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateWithParam(courtAndRoomId, COURT_ROOM_NAME, fifthJuly)));
        UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateWithParam(courtAndRoomId, COURT_ROOM_NAME, fifthJuly.minusDays(1)));

        Hearing hearing = hearingOne.getHearing();
        hearing.setProsecutionCases(null);

        Queries.getHearingsByDatePollForMatch(courtAndRoomId, courtAndRoomId, fifthJuly.toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .withValue(HearingSummaries::getHearingLanguage, HearingLanguage.ENGLISH.name())
                                .with(HearingSummaries::getType, isBean(HearingType.class))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                ))
                        ))
        );
    }

    @Test
    public void initiateHearing_withMultipleCases() {

        HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), asList(randomUUID(), randomUUID()));
        value.put(randomUUID(), asList(randomUUID()));
        caseStructure.put(randomUUID(), value);
        caseStructure.put(randomUUID(), toMap(randomUUID(), asList(randomUUID(), randomUUID())));
        caseStructure.put(randomUUID(), toMap(randomUUID(), asList(randomUUID())));
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(),
                InitiateHearingCommand.initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build())));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearing.getCourtCentre().getName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, courtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, courtApplication.getApplicationReference())
                        ))
                        .with(Hearing::getProsecutionCases, MatcherUtil.getProsecutionCasesMatchers(hearingOne.getHearing().getProsecutionCases()))
                )
        );
//TODO court applications
        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                //.withValue(HearingSummaries::getJurisdictionType, hearing.getJurisdictionType())
                                .withValue(HearingSummaries::getReportingRestrictionReason, hearing.getReportingRestrictionReason())
                                .withValue(HearingSummaries::getHearingLanguage, ENGLISH.name())
                                .with(HearingSummaries::getCourtCentre, isBean(CourtCentre.class)
                                        .withValue(CourtCentre::getId, hearing.getCourtCentre().getId())
                                        .withValue(CourtCentre::getName, hearing.getCourtCentre().getName()))
                                .with(HearingSummaries::getType, isBean(HearingType.class)
                                        .withValue(HearingType::getId, hearing.getType().getId())
                                        .withValue(HearingType::getDescription, hearing.getType().getDescription()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .withValue(HearingDay::getSittingDay, hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC")))
                                        .withValue(HearingDay::getListedDurationMinutes, hearingDay.getListedDurationMinutes())
                                        .withValue(HearingDay::getListingSequence, hearingDay.getListingSequence())))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasProsecutionSummaries(hearing.getProsecutionCases()))
                                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                                        .withValue(CourtApplicationSummaries::getId, courtApplication.getId())
                                ))
                        ))
        );
    }

    public Matcher<Iterable<ProsecutionCaseSummaries>> hasProsecutionSummaries(final List<ProsecutionCase> prosecutionCases) {
        return hasItems(
                prosecutionCases.stream().map(
                        prosecutionCase -> hasProsecutionCaseSummary(prosecutionCase)
                ).toArray(BeanMatcher[]::new)
        );

    }

    public BeanMatcher<ProsecutionCaseSummaries> hasProsecutionCaseSummary(final ProsecutionCase prosecutionCase) {
        return isBean(ProsecutionCaseSummaries.class)
                .withValue(ProsecutionCaseSummaries::getId, prosecutionCase.getId())
                .with(ProsecutionCaseSummaries::getProsecutionCaseIdentifier,
                        isBean(ProsecutionCaseIdentifier.class)
                                .withValue(ProsecutionCaseIdentifier::getCaseURN, prosecutionCase.getProsecutionCaseIdentifier().getCaseURN())
                                .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityCode())
                                .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityId, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId())
                                .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityReference()))
                .with(ProsecutionCaseSummaries::getDefendants,
                        hasDefendantSummaries(prosecutionCase)
                );
    }

    public Matcher<Iterable<Defendants>> hasDefendantSummaries(final ProsecutionCase prosecutionCase) {
        return hasItems(prosecutionCase.getDefendants().stream().map(defendant ->
                isBean(Defendants.class)
                        .withValue(Defendants::getId, defendant.getId())
                        .withValue(Defendants::getFirstName, defendant.getPersonDefendant().getPersonDetails().getFirstName()))
                .toArray(BeanMatcher[]::new));
    }
}