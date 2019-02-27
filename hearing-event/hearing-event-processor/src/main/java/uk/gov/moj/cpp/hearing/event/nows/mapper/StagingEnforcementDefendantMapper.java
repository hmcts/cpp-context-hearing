package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.nonNull;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.Defendant;
import uk.gov.justice.json.schemas.staging.DocumentLanguage;
import uk.gov.justice.json.schemas.staging.HearingLanguage;
import uk.gov.justice.json.schemas.staging.Title;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class StagingEnforcementDefendantMapper {

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
                .withVehicleMake(null)
                .withWeeklyIncome(null)
                .build();
    }

    private Title setTitle(final uk.gov.justice.core.courts.Defendant defendant) {
        return nonNull(defendant.getLegalEntityDefendant()) ? Title.CO : convertTitle(defendant.getPersonDefendant().getPersonDetails().getTitle());
    }

    private Title convertTitle(uk.gov.justice.core.courts.Title title) {
        switch (title) {
            case MR:
                return Title.MR;
            case MRS:
                return Title.MRS;
            case MS:
                return Title.MS;
            case MISS:
                return Title.MISS;
        }

        return null;
    }

    private String setSurname(final uk.gov.justice.core.courts.Defendant defendant) {
        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getLastName();
        }

        return null;
    }

    private String setForenames(final uk.gov.justice.core.courts.Defendant defendant) {
        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getFirstName() + " " + defendant.getPersonDefendant().getPersonDetails().getMiddleName();
        }

        return null;
    }

    private LocalDate setDateOfBirth(final uk.gov.justice.core.courts.Defendant defendant) {
        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getDateOfBirth();
        }

        return null;
    }

    private String setCompanyName(final uk.gov.justice.core.courts.Defendant defendant) {
        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getName();
        }

        return null;
    }

    private String setAddress1(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress1();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress1();
        }

        return null;
    }

    private String setAddress2(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress2();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress2();
        }

        return null;
    }

    private String setAddress3(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress3();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress3();
        }

        return null;
    }

    private String setAddress4(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress4();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress4();
        }

        return null;
    }

    private String setAddress5(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress5();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress5();
        }

        return null;
    }

    private String setPostcode(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getAddress().getPostcode();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getAddress().getPostcode();
        }

        return null;
    }

    private String setNationalInsuranceNumber(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getNationalInsuranceNumber();
        }

        return null;
    }

    private String setTelephoneNumberHome(final uk.gov.justice.core.courts.Defendant defendant) {
        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getContact().getHome();
        }

        return null;
    }

    private String setTelephoneNumberBusiness(final uk.gov.justice.core.courts.Defendant defendant) {
        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getContact().getWork();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getContact().getWork();
        }

        return null;
    }

    private String setTelephoneNumberMobile(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getContact().getMobile();
        }

        return null;
    }

    private String setEmailAddress1(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getContact().getPrimaryEmail();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getContact().getPrimaryEmail();
        }

        return null;
    }

    private String setEmailAddress2(final uk.gov.justice.core.courts.Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return defendant.getPersonDefendant().getPersonDetails().getContact().getSecondaryEmail();
        }

        if (nonNull(defendant.getLegalEntityDefendant())) {
            return defendant.getLegalEntityDefendant().getOrganisation().getContact().getSecondaryEmail();
        }

        return null;
    }

    private LocalDate setDateOfSentence(final Now nows, final List<SharedResultLine> sharedResultLines) {

        final List<UUID> listOfSharedResultIds = nows.getRequestedMaterials().stream()
                .flatMap(requestedMaterials -> requestedMaterials.getNowResults().stream())
                .map(NowVariantResult::getSharedResultId)
                .collect(Collectors.toList());

        final SharedResultLine sharedResultLine = sharedResultLines.stream()
                .filter(line -> listOfSharedResultIds.contains(line.getId()))
                .filter(line -> line.getOrderedDate() != null)
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
