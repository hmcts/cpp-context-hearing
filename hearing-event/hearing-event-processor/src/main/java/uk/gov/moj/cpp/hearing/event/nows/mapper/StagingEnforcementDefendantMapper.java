package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.Aliases;
import uk.gov.justice.json.schemas.staging.Defendant;
import uk.gov.justice.json.schemas.staging.DocumentLanguage;
import uk.gov.justice.json.schemas.staging.HearingLanguage;
import uk.gov.justice.json.schemas.staging.Title;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1168"})
class StagingEnforcementDefendantMapper {

    private static final String SPACE = " ";
    private final List<SharedResultLine> sharedResultLines;

    private final uk.gov.justice.core.courts.Defendant defendant;

    private final Now now;

    private final String hearingLanguage;

    StagingEnforcementDefendantMapper(final uk.gov.justice.core.courts.Defendant defendant,
                                      final Now now,
                                      final String hearingLanguage,
                                      final List<SharedResultLine> sharedResultLines) {
        this.defendant = defendant;
        this.now = now;
        this.hearingLanguage = hearingLanguage;
        this.sharedResultLines = new ArrayList<>(sharedResultLines);
    }

    Defendant createDefendant() {
        return Defendant.defendant()
                .withAddress1(setAddress1(defendant))
                .withAddress2(setAddress2(defendant))
                .withAddress3(setAddress3(defendant))
                .withAddress4(setAddress4(defendant))
                .withAddress5(setAddress5(defendant))
                .withBenefitsTypes(null)
                .withCompanyName(setCompanyName(defendant))
                .withDateOfBirth(setDateOfBirth(defendant))
                .withDateOfSentence(setDateOfSentence(now, sharedResultLines))
                .withDocumentLanguage(setDocumentLanguage(now))
                .withEmailAddress1(setEmailAddress1(defendant))
                .withEmailAddress2(setEmailAddress2(defendant))
                .withForenames(setForenames(defendant))
                .withHearingLanguage(setHearingLanguage(this.hearingLanguage))
                .withNationalInsuranceNumber(setNationalInsuranceNumber(defendant))
                .withPostcode(setPostcode(defendant))
                .withStatementOfMeansProvided(null)
                .withSurname(setSurname(defendant))
                .withTelephoneNumberBusiness(setTelephoneNumberBusiness(defendant))
                .withTelephoneNumberHome(setTelephoneNumberHome(defendant))
                .withTelephoneNumberMobile(setTelephoneNumberMobile(defendant))
                .withTitle(setTitle(defendant))
                .withVehicleMake(null)
                .withVehicleRegistrationMark(setVehicleRegistrationMark(defendant))
                .withWeeklyIncome(null)
                .withAliases(setAliases(defendant))
                .build();
    }

    private Title setTitle(final uk.gov.justice.core.courts.Defendant defendant) {
        return nonNull(defendant.getLegalEntityDefendant()) ? Title.CO : convertTitle(defendant.getPersonDefendant().getPersonDetails().getTitle());
    }

    private String middleNameToInitial(String middleName) {
        if (middleName == null || middleName.trim().length() == 0) {
            return null;
        } else {
            return middleName.substring(0, 1).toUpperCase();
        }
    }

    private List<Aliases> setAliases(final uk.gov.justice.core.courts.Defendant defendant) {
        if (isEmpty(defendant.getAliases())) {
            return null;
        } else {
            return defendant.getAliases().stream().map(a -> new Aliases(a.getFirstName(), middleNameToInitial(a.getMiddleName()), a.getLastName(), null)).collect(Collectors.toList());
        }
    }

    private Title convertTitle(uk.gov.justice.core.courts.Title title) {
        return Optional.ofNullable(title)
                .map(t -> {
                    switch (t) {
                        case MR:
                            return Title.MR;
                        case MRS:
                            return Title.MRS;
                        case MS:
                            return Title.MS;
                        case MISS:
                            return Title.MISS;
                        default:
                            return Title.MR;
                    }
                })
                .orElse(Title.MR);

    }

    private String setSurname(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getLastName)
                .orElse(null);
    }

    private String setForenames(final uk.gov.justice.core.courts.Defendant defendant) {
        return Stream.of(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                        .map(PersonDefendant::getPersonDetails)
                        .map(Person::getFirstName)
                        .orElse(null),
                Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                        .map(PersonDefendant::getPersonDetails)
                        .map(Person::getMiddleName)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(SPACE));
    }

    private LocalDate setDateOfBirth(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getDateOfBirth)
                .orElse(null);
    }

    private String setCompanyName(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                .map(LegalEntityDefendant::getOrganisation)
                .map(Organisation::getName)
                .orElse(null);

    }

    private String setAddress1(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getAddress)
                .map(Address::getAddress1)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getAddress)
                        .map(Address::getAddress1)
                        .orElse(null));
    }

    private String setAddress2(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getAddress)
                .map(Address::getAddress2)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getAddress)
                        .map(Address::getAddress2)
                        .orElse(null));
    }

    private String setAddress3(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getAddress)
                .map(Address::getAddress3)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getAddress)
                        .map(Address::getAddress3)
                        .orElse(null));
    }

    private String setAddress4(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getAddress)
                .map(Address::getAddress4)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getAddress)
                        .map(Address::getAddress4)
                        .orElse(null));
    }

    private String setAddress5(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getAddress)
                .map(Address::getAddress5)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getAddress)
                        .map(Address::getAddress5)
                        .orElse(null));
    }

    private String setPostcode(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getAddress)
                .map(Address::getPostcode)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getAddress)
                        .map(Address::getPostcode)
                        .orElse(null));
    }

    private String setNationalInsuranceNumber(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getNationalInsuranceNumber)
                .orElse(null);
    }

    private String setTelephoneNumberHome(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getContact)
                .map(ContactNumber::getHome)
                .orElse(null);
    }

    private String setTelephoneNumberBusiness(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getContact)
                .map(ContactNumber::getWork)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getContact)
                        .map(ContactNumber::getWork)
                        .orElse(null));
    }

    private String setTelephoneNumberMobile(final uk.gov.justice.core.courts.Defendant defendant) {

        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getContact)
                .map(ContactNumber::getMobile)
                .orElse(null);

    }

    private String setVehicleRegistrationMark(uk.gov.justice.core.courts.Defendant defendant) {
//find first non null vehicle registration on first non null offence facts
        return defendant.getOffences().stream()
                .map(Offence::getOffenceFacts)
                .filter(Objects::nonNull)
                .map(OffenceFacts::getVehicleRegistration)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }


    private String setEmailAddress1(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getContact)
                .map(ContactNumber::getPrimaryEmail)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getContact)
                        .map(ContactNumber::getPrimaryEmail)
                        .orElse(null));

    }

    private String setEmailAddress2(final uk.gov.justice.core.courts.Defendant defendant) {
        return Optional.ofNullable(defendant)
                .map(uk.gov.justice.core.courts.Defendant::getPersonDefendant)
                .map(PersonDefendant::getPersonDetails)
                .map(Person::getContact)
                .map(ContactNumber::getSecondaryEmail)
                .orElse(Optional.ofNullable(defendant)
                        .map(uk.gov.justice.core.courts.Defendant::getLegalEntityDefendant)
                        .map(LegalEntityDefendant::getOrganisation)
                        .map(Organisation::getContact)
                        .map(ContactNumber::getSecondaryEmail)
                        .orElse(null));
    }

    private LocalDate setDateOfSentence(final Now nows, final List<SharedResultLine> sharedResultLines) {

        final List<UUID> listOfSharedResultIds = nows.getRequestedMaterials().stream()
                .flatMap(requestedMaterials -> requestedMaterials.getNowResults().stream())
                .map(NowVariantResult::getSharedResultId)
                .collect(Collectors.toList());

        final SharedResultLine sharedResultLine = sharedResultLines.stream()
                .filter(line -> listOfSharedResultIds.contains(line.getId()))
                .filter(line -> line.getOrderedDate() != null)
                .filter(r -> (isNull(r.getIsDeleted()) || !r.getIsDeleted()))
                .findFirst()
                .orElse(null);

        if (nonNull(sharedResultLine)) {
            return sharedResultLine.getOrderedDate();
        }

        return null;
    }

    private DocumentLanguage setDocumentLanguage(final Now nows) {
        return DocumentLanguage.valueOf(nows.getDocumentationLanguage().toString());
    }

    private HearingLanguage setHearingLanguage(final String hearingLanguage) {
        return HearingLanguage.valueOf(hearingLanguage);
    }
}
