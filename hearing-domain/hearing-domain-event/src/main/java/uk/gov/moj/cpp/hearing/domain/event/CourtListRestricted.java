package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.event.court-list-restricted")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
public class CourtListRestricted implements Serializable {
    private static final long serialVersionUID = -4599520303773075427L;

    private List<UUID> caseIds;

    private List<UUID> courtApplicationApplicantIds;

    private List<UUID> courtApplicationIds;

    private List<UUID> courtApplicationRespondentIds;

    private String courtApplicationType;

    private List<UUID> defendantIds;

    private UUID hearingId;

    private List<UUID> offenceIds;

    private Boolean restrictCourtList;

    public CourtListRestricted(final List<UUID> caseIds, final List<UUID> courtApplicationApplicantIds, final List<UUID> courtApplicationIds, final List<UUID> courtApplicationRespondentIds, final String courtApplicationType, final List<UUID> defendantIds, final UUID hearingId, final List<UUID> offenceIds, final Boolean restrictCourtList) {
        this.caseIds = caseIds;
        this.courtApplicationApplicantIds = courtApplicationApplicantIds;
        this.courtApplicationIds = courtApplicationIds;
        this.courtApplicationRespondentIds = courtApplicationRespondentIds;
        this.courtApplicationType = courtApplicationType;
        this.defendantIds = defendantIds;
        this.hearingId = hearingId;
        this.offenceIds = offenceIds;
        this.restrictCourtList = restrictCourtList;
    }

    public List<UUID> getCaseIds() {
        return caseIds;
    }

    public List<UUID> getCourtApplicationApplicantIds() {
        return courtApplicationApplicantIds;
    }

    public List<UUID> getCourtApplicationIds() {
        return courtApplicationIds;
    }

    public List<UUID> getCourtApplicationRespondentIds() {
        return courtApplicationRespondentIds;
    }

    public String getCourtApplicationType() {
        return courtApplicationType;
    }

    public List<UUID> getDefendantIds() {
        return defendantIds;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<UUID> getOffenceIds() {
        return offenceIds;
    }

    public Boolean getRestrictCourtList() {
        return restrictCourtList;
    }

    public static Builder courtListRestricted() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "CourtListRestricted{" +
                "caseIds='" + caseIds + "'," +
                "courtApplicationApplicantIds='" + courtApplicationApplicantIds + "'," +
                "courtApplicationIds='" + courtApplicationIds + "'," +
                "courtApplicationRespondentIds='" + courtApplicationRespondentIds + "'," +
                "courtApplicationType='" + courtApplicationType + "'," +
                "defendantIds='" + defendantIds + "'," +
                "hearingId='" + hearingId + "'," +
                "offenceIds='" + offenceIds + "'," +
                "restrictCourtList='" + restrictCourtList + "'" +
                "}";
    }

    public CourtListRestricted setCaseIds(List<UUID> caseIds) {
        this.caseIds = caseIds;
        return this;
    }

    public CourtListRestricted setCourtApplicationApplicantIds(List<UUID> courtApplicationApplicantIds) {
        this.courtApplicationApplicantIds = courtApplicationApplicantIds;
        return this;
    }

    public CourtListRestricted setCourtApplicationIds(List<UUID> courtApplicationIds) {
        this.courtApplicationIds = courtApplicationIds;
        return this;
    }

    public CourtListRestricted setCourtApplicationRespondentIds(List<UUID> courtApplicationRespondentIds) {
        this.courtApplicationRespondentIds = courtApplicationRespondentIds;
        return this;
    }

    public CourtListRestricted setCourtApplicationType(String courtApplicationType) {
        this.courtApplicationType = courtApplicationType;
        return this;
    }

    public CourtListRestricted setDefendantIds(List<UUID> defendantIds) {
        this.defendantIds = defendantIds;
        return this;
    }

    public CourtListRestricted setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public CourtListRestricted setOffenceIds(List<UUID> offenceIds) {
        this.offenceIds = offenceIds;
        return this;
    }

    public CourtListRestricted setRestrictCourtList(Boolean restrictCourtList) {
        this.restrictCourtList = restrictCourtList;
        return this;
    }

    public static class Builder {
        private List<UUID> caseIds;

        private List<UUID> courtApplicationApplicantIds;

        private List<UUID> courtApplicationIds;

        private List<UUID> courtApplicationRespondentIds;

        private String courtApplicationType;

        private List<UUID> defendantIds;

        private UUID hearingId;

        private List<UUID> offenceIds;

        private Boolean restrictCourtList;

        public Builder withCaseIds(final List<UUID> caseIds) {
            this.caseIds = caseIds;
            return this;
        }

        public Builder withCourtApplicationApplicantIds(final List<UUID> courtApplicationApplicantIds) {
            this.courtApplicationApplicantIds = courtApplicationApplicantIds;
            return this;
        }

        public Builder withCourtApplicationIds(final List<UUID> courtApplicationIds) {
            this.courtApplicationIds = courtApplicationIds;
            return this;
        }

        public Builder withCourtApplicationRespondentIds(final List<UUID> courtApplicationRespondentIds) {
            this.courtApplicationRespondentIds = courtApplicationRespondentIds;
            return this;
        }

        public Builder withCourtApplicationType(final String courtApplicationType) {
            this.courtApplicationType = courtApplicationType;
            return this;
        }

        public Builder withDefendantIds(final List<UUID> defendantIds) {
            this.defendantIds = defendantIds;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withOffenceIds(final List<UUID> offenceIds) {
            this.offenceIds = offenceIds;
            return this;
        }

        public Builder withRestrictCourtList(final Boolean restrictCourtList) {
            this.restrictCourtList = restrictCourtList;
            return this;
        }

        public Builder withValuesFrom(final CourtListRestricted courtListRestricted) {
            this.caseIds = courtListRestricted.getCaseIds();
            this.courtApplicationApplicantIds = courtListRestricted.getCourtApplicationApplicantIds();
            this.courtApplicationIds = courtListRestricted.getCourtApplicationIds();
            this.courtApplicationRespondentIds = courtListRestricted.getCourtApplicationRespondentIds();
            this.courtApplicationType = courtListRestricted.getCourtApplicationType();
            this.defendantIds = courtListRestricted.getDefendantIds();
            this.hearingId = courtListRestricted.getHearingId();
            this.offenceIds = courtListRestricted.getOffenceIds();
            this.restrictCourtList = courtListRestricted.getRestrictCourtList();
            return this;
        }

        public CourtListRestricted build() {
            return new CourtListRestricted(caseIds, courtApplicationApplicantIds, courtApplicationIds, courtApplicationRespondentIds, courtApplicationType, defendantIds, hearingId, offenceIds, restrictCourtList);
        }
    }
}
