package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.query.CaseByDefendant;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.QueryParam;

public abstract class CaseByDefendantRepository extends AbstractEntityRepository<CaseByDefendant, UUID> {

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String DATE_OF_BIRTH = "dateOfBirth";
    private static final String HEARING_DATE = "hearingDate";
    private static final String CASE_IDS = "caseIds";
    private static final String ORGANISATION_NAME = "organisationName";

    public List<CaseByDefendant> getCasesByPersonDefendant(
            @QueryParam(FIRST_NAME) final String firstName,
            @QueryParam(LAST_NAME) final String lastName,
            @QueryParam(DATE_OF_BIRTH) final LocalDate dateOfBirth,
            @QueryParam(HEARING_DATE) final LocalDate hearingDate,
            @QueryParam(CASE_IDS) final Set<UUID> caseIds) {

        if (!caseIds.isEmpty()) {
            return this.entityManager().createNamedQuery(
                    CaseByDefendant.FIND_CASE_BY_PERSON_DEFENDANT_WITH_CASE_ID, CaseByDefendant.class)
                    .setParameter(FIRST_NAME, firstName.toUpperCase())
                    .setParameter(LAST_NAME, lastName.toUpperCase())
                    .setParameter(DATE_OF_BIRTH, dateOfBirth)
                    .setParameter(HEARING_DATE, hearingDate)
                    .setParameter(CASE_IDS, caseIds)
                    .getResultList();
        } else {
            return this.entityManager().createNamedQuery(
                    CaseByDefendant.FIND_CASE_BY_PERSON_DEFENDANT_WITHOUT_CASE_ID, CaseByDefendant.class)
                    .setParameter(FIRST_NAME, firstName.toUpperCase())
                    .setParameter(LAST_NAME, lastName.toUpperCase())
                    .setParameter(DATE_OF_BIRTH, dateOfBirth)
                    .setParameter(HEARING_DATE, hearingDate)
                    .getResultList();
        }
    }

    public List<CaseByDefendant> getCasesByOrganisationDefendant(
            @QueryParam(ORGANISATION_NAME) final String organisationName,
            @QueryParam(HEARING_DATE) final LocalDate hearingDate,
            @QueryParam(CASE_IDS) final Set<UUID> caseIds) {

        if (!caseIds.isEmpty()) {
            return this.entityManager().createNamedQuery(
                    CaseByDefendant.FIND_CASE_BY_ORGANISATION_DEFENDANT_WITH_CASE_ID, CaseByDefendant.class)
                    .setParameter(ORGANISATION_NAME, organisationName.toUpperCase())
                    .setParameter(HEARING_DATE, hearingDate)
                    .setParameter(CASE_IDS, caseIds)
                    .getResultList();
        } else {
            return this.entityManager().createNamedQuery(
                    CaseByDefendant.FIND_CASE_BY_ORGANISATION_DEFENDANT_WITHOUT_CASE_ID, CaseByDefendant.class)
                    .setParameter(ORGANISATION_NAME, organisationName.toUpperCase())
                    .setParameter(HEARING_DATE, hearingDate)
                    .getResultList();
        }
    }
}