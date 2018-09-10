package uk.gov.moj.cpp.hearing.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;

import uk.gov.justice.json.schemas.core.ContactNumber;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.IndicatedPlea;
import uk.gov.justice.json.schemas.core.LegalEntityDefendant;
import uk.gov.justice.json.schemas.core.NotifiedPlea;
import uk.gov.justice.json.schemas.core.Person;
import uk.gov.justice.json.schemas.core.PersonDefendant;
import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.justice.json.schemas.core.ReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HearingObjectMapper {

    public Hearing buildHearingEntity(uk.gov.justice.json.schemas.core.Hearing hearing) {

        final HearingLanguage language = ofNullable(hearing.getHearingLanguage()).orElse(HearingLanguage.ENGLISH);

        final Hearing hearingEntity = new Hearing();
        hearingEntity.setId(hearing.getId());
        hearingEntity.setHearingType(buildHearingType(hearing.getType()));
        hearingEntity.setCourtCentre(buildCourtCentre(hearing.getCourtCentre()));
        hearingEntity.setJurisdictionType(hearing.getJurisdictionType());
        hearingEntity.setReportingRestrictionReason(hearing.getReportingRestrictionReason());
        hearingEntity.setHearingLanguage(language);
        hearingEntity.setHasSharedResults(hearing.getHasSharedResults());
        hearingEntity.setHearingDays(buildHearingDay(hearing.getId(), hearing.getHearingDays()));
        hearingEntity.setJudicialRoles(buildJudicialRole(hearing.getId(), hearing.getJudiciary()));
        hearingEntity.setDefendantReferralReasons(buildDefendantReferralReasons(hearing.getId(), hearing.getDefendantReferralReasons()));
        hearingEntity.setProsecutionCases(buildProsecutionCases(hearing.getId(), hearing.getProsecutionCases()));

        return hearingEntity;
    }

    @SuppressWarnings({"squid:S1172"})
    private HearingType buildHearingType(uk.gov.justice.json.schemas.core.HearingType type) {
        return null;
    }

    @SuppressWarnings({"squid:S1172"})
    private CourtCentre buildCourtCentre(uk.gov.justice.json.schemas.core.CourtCentre courtCentre) {
        return null;
    }

    private List<ProsecutionCase> buildProsecutionCases(UUID hearingId, List<uk.gov.justice.json.schemas.core.ProsecutionCase> prosecutionCases) {

        final List<ProsecutionCase> prosecutionCasesList = new ArrayList<>();

        prosecutionCases.forEach(prosecutionCase -> {
            ProsecutionCase prosecutionCaseEntity = new ProsecutionCase();
            prosecutionCaseEntity.setId(new HearingSnapshotKey(prosecutionCase.getId(), hearingId));
            prosecutionCaseEntity.setProsecutionCaseIdentifier(buildProsecutionCaseIdentifier(prosecutionCase.getProsecutionCaseIdentifier()));
            prosecutionCaseEntity.setOriginatingOrganisation(prosecutionCase.getOriginatingOrganisation());
            prosecutionCaseEntity.setInitiationCode(prosecutionCase.getInitiationCode());
            prosecutionCaseEntity.setCaseStatus(prosecutionCase.getCaseStatus());
            prosecutionCaseEntity.setStatementOfFacts(prosecutionCase.getStatementOfFacts());
            prosecutionCaseEntity.setStatementOfFactsWelsh(prosecutionCase.getStatementOfFactsWelsh());
            prosecutionCaseEntity.setDefendants(buildDefendants(hearingId, prosecutionCase.getDefendants()));
            prosecutionCasesList.add(prosecutionCaseEntity);
        });

        return prosecutionCasesList;
    }

    private ProsecutionCaseIdentifier buildProsecutionCaseIdentifier(uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier prosecutionCaseIdentifier) {
        ProsecutionCaseIdentifier entity = new ProsecutionCaseIdentifier();
        entity.setCaseURN(prosecutionCaseIdentifier.getCaseURN());
        entity.setProsecutionAuthorityReference(prosecutionCaseIdentifier.getProsecutionAuthorityReference());
        entity.setProsecutionAuthorityId(prosecutionCaseIdentifier.getProsecutionAuthorityId());
        entity.setProsecutionAuthorityCode(prosecutionCaseIdentifier.getProsecutionAuthorityCode());
        return entity;
    }

    @SuppressWarnings({"squid:S1188"})
    private List<Defendant> buildDefendants(UUID hearingId, List<uk.gov.justice.json.schemas.core.Defendant> defendants) {

        final List<Defendant> defendantList = new ArrayList<>();

        defendants.forEach(defendant -> {
            final PersonDefendant personDefendant = defendant.getPersonDefendant();
            final LegalEntityDefendant legalEntityDefendant = ofNullable(defendant.getLegalEntityDefendant()).orElse(null);
            final Defendant defendantEntity = new Defendant();
            defendantEntity.setId(new HearingSnapshotKey(defendant.getId(), hearingId));
            defendantEntity.setProsecutionCaseId(defendant.getProsecutionCaseId());
            defendantEntity.setNumberOfPreviousConvictionsCited(defendant.getNumberOfPreviousConvictionsCited());
            defendantEntity.setProsecutionAuthorityReference(defendant.getProsecutionAuthorityReference());
            defendantEntity.setWitnessStatement(defendant.getWitnessStatement());
            defendantEntity.setWitnessStatementWelsh(defendant.getWitnessStatementWelsh());
            defendantEntity.setMitigation(defendant.getMitigation());
            defendantEntity.setMitigationWelsh(defendant.getMitigationWelsh());
            defendantEntity.setOffences(buildOffences(hearingId, defendant.getId(), defendant.getOffences()));
            defendantEntity.setAssociatedPersons(buildAssociatedPersons(hearingId, defendant.getAssociatedPersons()));
            defendantEntity.setDefenceOrganisation(buildOrganisation(defendant.getDefenceOrganisation()));
            if (nonNull(legalEntityDefendant)) {
                defendantEntity.setLegalEntityOrganisation(buildOrganisation(legalEntityDefendant.getOrganisation()));
            }
            if (nonNull(personDefendant)) {
                defendantEntity.setPersonDefendant(buildPersonDefendant(personDefendant));
            }
            defendantList.add(defendantEntity);
        });

        return defendantList;
    }

    private uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant buildPersonDefendant(PersonDefendant personDefendant) {
        final Person personDetails = personDefendant.getPersonDetails();
        uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant personDefendantEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant();
        personDefendantEntity.setPersonDetails(buildPersonDetails(personDetails));
        personDefendantEntity.setBailStatus(personDefendant.getBailStatus());
        personDefendantEntity.setCustodyTimeLimit(personDefendant.getCustodyTimeLimit());
        personDefendantEntity.setPerceivedBirthYear(personDefendant.getPerceivedBirthYear());
        personDefendantEntity.setObservedEthnicityCode(personDefendant.getObservedEthnicityCode());
        personDefendantEntity.setObservedEthnicityId(personDefendant.getObservedEthnicityId());
        personDefendantEntity.setSelfDefinedEthnicityId(personDefendant.getSelfDefinedEthnicityId());
        personDefendantEntity.setSelfDefinedEthnicityCode(personDefendant.getSelfDefinedEthnicityCode());
        personDefendantEntity.setDriverNumber(personDefendant.getDriverNumber());
        personDefendantEntity.setPncId(personDefendant.getPncId());
        personDefendantEntity.setArrestSummonsNumber(personDefendant.getArrestSummonsNumber());
        personDefendantEntity.setAliases(personDefendant.getAliases().stream().collect(Collectors.joining(", ")));
        personDefendantEntity.setEmployerOrganisation(buildOrganisation(personDefendant.getEmployerOrganisation()));
        personDefendantEntity.setEmployerPayrollReference(personDefendant.getEmployerPayrollReference());
        return personDefendantEntity;
    }

    private uk.gov.moj.cpp.hearing.persist.entity.ha.Person buildPersonDetails(Person personDetails) {
        uk.gov.moj.cpp.hearing.persist.entity.ha.Person person = new uk.gov.moj.cpp.hearing.persist.entity.ha.Person();
        person.setTitle(personDetails.getTitle());
        person.setFirstName(personDetails.getFirstName());
        person.setMiddleName(personDetails.getMiddleName());
        person.setLastName(personDetails.getLastName());
        person.setDateOfBirth(personDetails.getDateOfBirth());
        person.setNationalityId(personDetails.getNationalityId());
        person.setNationalityCode(personDetails.getNationalityCode());
        person.setAdditionalNationalityId(personDetails.getAdditionalNationalityId());
        person.setAdditionalNationalityCode(personDetails.getAdditionalNationalityCode());
        person.setDisabilityStatus(personDetails.getDisabilityStatus());
        person.setEthnicityId(personDetails.getEthnicityId());
        person.setEthnicity(personDetails.getEthnicity());
        person.setGender(personDetails.getGender());
        person.setInterpreterLanguageNeeds(personDetails.getInterpreterLanguageNeeds());
        person.setDocumentationLanguageNeeds(personDetails.getDocumentationLanguageNeeds());
        person.setNationalInsuranceNumber(personDetails.getNationalInsuranceNumber());
        person.setOccupation(personDetails.getOccupation());
        person.setOccupationCode(personDetails.getOccupationCode());
        person.setSpecificRequirements(personDetails.getSpecificRequirements());
        person.setAddress(buildAddress(personDetails.getAddress()));
        person.setContact(buildContact(personDetails.getContact()));
        return person;
    }

    @SuppressWarnings({"squid:S1188"})
    private List<AssociatedPerson> buildAssociatedPersons(UUID hearingId, List<uk.gov.justice.json.schemas.core.AssociatedPerson> associatedPersons) {

        final List<AssociatedPerson> associatedPersonList = new ArrayList<>();

        if (isNull(associatedPersons)) {
            return associatedPersonList;
        }

        associatedPersons.forEach(associatedPerson -> {
            final Person person = associatedPerson.getPerson();
            final AssociatedPerson associatedPersonEntity = new AssociatedPerson();
            associatedPersonEntity.setId(new HearingSnapshotKey(randomUUID(), hearingId));
            associatedPersonEntity.setRole(associatedPerson.getRole());
            associatedPersonEntity.setTitle(person.getTitle());
            associatedPersonEntity.setFirstName(person.getFirstName());
            associatedPersonEntity.setMiddleName(person.getMiddleName());
            associatedPersonEntity.setLastName(person.getLastName());
            associatedPersonEntity.setDateOfBirth(person.getDateOfBirth());
            associatedPersonEntity.setNationalityId(person.getNationalityId());
            associatedPersonEntity.setNationalityCode(person.getNationalityCode());
            associatedPersonEntity.setAdditionalNationalityId(person.getAdditionalNationalityId());
            associatedPersonEntity.setAdditionalNationalityCode(person.getAdditionalNationalityCode());
            associatedPersonEntity.setDisabilityStatus(person.getDisabilityStatus());
            associatedPersonEntity.setEthnicityId(person.getEthnicityId());
            associatedPersonEntity.setEthnicity(person.getEthnicity());
            associatedPersonEntity.setGender(person.getGender());
            associatedPersonEntity.setInterpreterLanguageNeeds(person.getInterpreterLanguageNeeds());
            associatedPersonEntity.setDocumentationLanguageNeeds(person.getDocumentationLanguageNeeds());
            associatedPersonEntity.setNationalInsuranceNumber(person.getNationalInsuranceNumber());
            associatedPersonEntity.setOccupation(person.getOccupation());
            associatedPersonEntity.setOccupationCode(person.getOccupationCode());
            associatedPersonEntity.setSpecificRequirements(person.getSpecificRequirements());
            associatedPersonEntity.setAddress(buildAddress(person.getAddress()));
            associatedPersonEntity.setContact(buildContact(person.getContact()));
            associatedPersonList.add(associatedPersonEntity);
        });

        return associatedPersonList;
    }


    @SuppressWarnings({"squid:CommentedOutCodeLine", "squid:S1188"})
    private List<Offence> buildOffences(final UUID hearingId, final UUID defendantId, final List<uk.gov.justice.json.schemas.core.Offence> offences) {

        final List<Offence> offenceList = new ArrayList<>();

        offences.forEach(offence -> {

            final NotifiedPlea notifiedPlea = ofNullable(offence.getNotifiedPlea()).orElse(null);

            final IndicatedPlea indicatedPlea = ofNullable(offence.getIndicatedPlea()).orElse(null);

            final Offence offenceEntity = new Offence();
            offenceEntity.setId(new HearingSnapshotKey(offence.getId(), hearingId));
            offenceEntity.setOffenceDefinitionId(offence.getOffenceDefinitionId());
            offenceEntity.setDefendantId(defendantId);
            offenceEntity.setOffenceCode(offence.getOffenceCode());
            offenceEntity.setOffenceTitle(offence.getOffenceTitle());
            offenceEntity.setOffenceTitleWelsh(offence.getOffenceTitleWelsh());
            offenceEntity.setOffenceLegislation(offence.getOffenceLegislation());
            offenceEntity.setOffenceLegislationWelsh(offence.getOffenceLegislationWelsh());
            offenceEntity.setModeOfTrial(offence.getModeOfTrial());
            offenceEntity.setWording(offence.getWording());
            offenceEntity.setWordingWelsh(offence.getWordingWelsh());
            offenceEntity.setStartDate(offence.getStartDate());
            offenceEntity.setEndDate(offence.getEndDate());
            offenceEntity.setArrestDate(offence.getArrestDate());
            offenceEntity.setChargeDate(offence.getChargeDate());
            offenceEntity.setOrderIndex(offence.getOrderIndex());
            offenceEntity.setCount(offence.getCount());
            offenceEntity.setConvictionDate(offence.getConvictionDate());

            if (nonNull(notifiedPlea)) {
                /*
                offenceEntity.setNotifiedPleaDate(notifiedPlea.getNotifiedPleaDate());
                offenceEntity.setNotifiedPleaValue(notifiedPlea.getNotifiedPleaValue().toString());
                */
            }

            if (nonNull(indicatedPlea)) {
                /*
                offenceEntity.setIndicatedPleaDate(indicatedPlea.getIndicatedPleaDate());
                offenceEntity.setIndicatedPleaSource(ofNullable(indicatedPlea.getSource()).orElse(Source.ONLINE).toString());
                offenceEntity.setIndicatedPleaValue(ofNullable(indicatedPlea.getIndicatedPleaValue()).orElse(IndicatedPleaValue.INDICATED_GUILTY).toString());

                offenceEntity.setCourtDecision(indicatedPlea.getAllocationDecision().getCourtDecision().toString());
                offenceEntity.setProsecutionRepresentation(indicatedPlea.getAllocationDecision().getProsecutionRepresentation().toString());
                offenceEntity.setDefendantRepresentation(indicatedPlea.getAllocationDecision().getDefendantRepresentation().toString());
                offenceEntity.setIndicationOfSentence(indicatedPlea.getAllocationDecision().getIndicationOfSentence());
                */
            }

            if (nonNull(offence.getOffenceFacts())) {

                OffenceFacts of = new OffenceFacts();
                of.setVehicleRegistration(offence.getOffenceFacts().getVehicleRegistration());
                of.setAlcoholReadingAmount(offence.getOffenceFacts().getAlcoholReadingAmount());
                of.setAlcoholReadingMethod(offence.getOffenceFacts().getAlcoholReadingMethod());
                offenceEntity.setOffenceFacts(of);
            }

            offenceList.add(offenceEntity);
        });

        return offenceList;
    }

    @SuppressWarnings({"squid:S1172"})
    private Organisation buildOrganisation(uk.gov.justice.json.schemas.core.Organisation organisation) {
        return null;
    }

    @SuppressWarnings({"squid:S1172"})
    private Contact buildContact(ContactNumber contactNumber) {
        return null;
    }

    @SuppressWarnings({"squid:S1172"})
    private Address buildAddress(final uk.gov.justice.json.schemas.core.Address address) {

        return null;

    }

    @SuppressWarnings({"squid:S1172"})
    private List<DefendantReferralReason> buildDefendantReferralReasons(final UUID hearingId, final List<ReferralReason> referralReasons) {
        return Collections.emptyList();
    }

    @SuppressWarnings({"squid:S1172"})
    private List<JudicialRole> buildJudicialRole(final UUID hearingId, final List<uk.gov.justice.json.schemas.core.JudicialRole> judiciary) {
        return Collections.emptyList();
    }

    @SuppressWarnings({"squid:S1172"})
    private List<HearingDay> buildHearingDay(UUID hearingId, List<uk.gov.justice.json.schemas.core.HearingDay> hearingDays) {
        return Collections.emptyList();
    }
}
