package uk.gov.moj.cpp.hearing.query;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;

@Entity(name = "CaseByDefendant")
@NamedNativeQueries({
        @NamedNativeQuery(
                name = CaseByDefendant.FIND_CASE_BY_PERSON_DEFENDANT_WITH_CASE_ID,
                query = CaseByDefendant.QUERY + "AND c.id in(:caseIds) "+
                        "AND upper(d.first_name)= :firstName AND upper(d.last_name) = :lastName AND d.date_of_birth= :dateOfBirth ",
                resultClass = CaseByDefendant.class
        ),
        @NamedNativeQuery(
                name = CaseByDefendant.FIND_CASE_BY_PERSON_DEFENDANT_WITHOUT_CASE_ID,
                query = CaseByDefendant.QUERY + "AND upper(d.first_name) = :firstName AND upper(d.last_name) = :lastName AND d.date_of_birth= :dateOfBirth ",
                resultClass = CaseByDefendant.class
        ),
        @NamedNativeQuery(
                name = CaseByDefendant.FIND_CASE_BY_ORGANISATION_DEFENDANT_WITH_CASE_ID,
                query = CaseByDefendant.QUERY + "AND c.id in(:caseIds) "+
                        "AND upper(d.leg_ent_org_name) = :organisationName ",
                resultClass = CaseByDefendant.class
        ),
        @NamedNativeQuery(
                name = CaseByDefendant.FIND_CASE_BY_ORGANISATION_DEFENDANT_WITHOUT_CASE_ID,
                query = CaseByDefendant.QUERY + "AND upper(d.leg_ent_org_name) = :organisationName ",
                resultClass = CaseByDefendant.class
        )
})

public class CaseByDefendant implements Serializable {

    public static final String FIND_CASE_BY_PERSON_DEFENDANT_WITH_CASE_ID = "findCaseByPersonDefendantWithCaseId";
    public static final String FIND_CASE_BY_PERSON_DEFENDANT_WITHOUT_CASE_ID = "findCaseByPersonDefendantWithoutCaseId";
    public static final String FIND_CASE_BY_ORGANISATION_DEFENDANT_WITH_CASE_ID = "findCaseByOrganisationDefendantWithCaseId";
    public static final String FIND_CASE_BY_ORGANISATION_DEFENDANT_WITHOUT_CASE_ID = "findCaseByOrganisationDefendantWithoutCaseId";

    public static final String QUERY = "SELECT c.id as caseId, c.caseurn as urn " +
            "FROM ha_case c, ha_defendant d, ha_hearing h, ha_hearing_day hd " +
            "WHERE c.id = d.prosecution_case_id " +
            "AND c.hearing_id = h.id " +
            "AND hd.hearing_id = h.id " +
            "AND hd.date = :hearingDate ";

    @Id
    @Column(name = "caseId", nullable = false)
    private UUID caseId;

    @Column(name = "urn")
    private String urn;


    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(final UUID caseId) {
        this.caseId = caseId;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(final String urn) {
        this.urn = urn;
    }

    public static Builder caseByDefendant() {
        return new Builder();
    }

    public static final class Builder {

        private UUID caseId;
        private String urn;

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withUrn(String urn) {
            this.urn = urn;
            return this;
        }

        public CaseByDefendant build() {
            final CaseByDefendant caseByDefendant = new CaseByDefendant();
            caseByDefendant.setCaseId(caseId);
            caseByDefendant.setUrn(urn);
            return caseByDefendant;
        }
    }
}
