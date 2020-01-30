package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.HearingEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"squid:S1067"})
public class CaseDetail implements Serializable {
    private static final long serialVersionUID = -4951785613141170679L;

    private String caseNumber;

    private String caseType;

    private String cppUrn;

    private List<Defendant> defendants;

    private String hearingType;

    private String judgeName;

    private String notBeforeTime;

    private HearingEvent hearingEvent;

    private List<UUID> linkedCaseIds;

    private List<DefenceCounsel> defenceCounsels;

    public CaseDetail(final String caseNumber, final String caseType, final String cppUrn, final List<Defendant> defendants, final String hearingType, final String judgeName, final String notBeforeTime, final  HearingEvent hearingEvent) {
        this.caseNumber = caseNumber;
        this.caseType = caseType;
        this.cppUrn = cppUrn;
        this.defendants = defendants;
        this.hearingType = hearingType;
        this.judgeName = judgeName;
        this.notBeforeTime = notBeforeTime;
        this.hearingEvent = hearingEvent;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public String getCaseType() {
        return caseType;
    }

    public String getCppUrn() {
        return cppUrn;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public String getHearingType() {
        return hearingType;
    }

    public String getJudgeName() {
        return judgeName;
    }

    public String getNotBeforeTime() {
        return notBeforeTime;
    }

    public HearingEvent getHearingEvent() {
        return hearingEvent;
    }

    @SuppressWarnings("squid:AssignmentInSubExpressionCheck")
    public List<UUID> getLinkedCaseIds() {
        return null == linkedCaseIds ? linkedCaseIds = new ArrayList<>() : linkedCaseIds;
    }

    public void setLinkedCaseIds(List<UUID> linkedCaseIds) {
        this.linkedCaseIds = linkedCaseIds;
    }

    public DefenceCounsel getDefenceCounsel() {
        return Optional.ofNullable(defenceCounsels).orElse(Collections.emptyList()).stream()
                .filter(defenceCounsel -> hearingEvent != null &&
                        defenceCounsel.getId().equals(hearingEvent.getDefenceCounselId()))
                .findFirst().orElse(null);
    }

    public void setDefenceCounsels(List<DefenceCounsel> defenceCounsels) {
        this.defenceCounsels = defenceCounsels;
    }

    public static Builder caseDetail() {
        return new CaseDetail.Builder();
    }

    @SuppressWarnings({"squid:S3776", "squid:S00121", "squid:S00122", "pmd:LocalVariableCouldBeFinal"} )
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CaseDetail that = (CaseDetail) o;

        if (caseNumber != null ? !caseNumber.equals(that.caseNumber) : that.caseNumber != null)
            return false;
        if (caseType != null ? !caseType.equals(that.caseType) : that.caseType != null)
            return false;
        if (cppUrn != null ? !cppUrn.equals(that.cppUrn) : that.cppUrn != null) return false;
        if (defendants != null ? !defendants.equals(that.defendants) : that.defendants != null)
            return false;
        if (hearingType != null ? !hearingType.equals(that.hearingType) : that.hearingType != null)
            return false;
        if (judgeName != null ? !judgeName.equals(that.judgeName) : that.judgeName != null)
            return false;
        return notBeforeTime != null ? notBeforeTime.equals(that.notBeforeTime) : that.notBeforeTime == null;
    }

    @SuppressWarnings({"squid:S3776", "squid:S00121", "squid:S00122"} )
    @Override
    public int hashCode() {
        int result = caseNumber != null ? caseNumber.hashCode() : 0;
        result = 31 * result + (caseType != null ? caseType.hashCode() : 0);
        result = 31 * result + (cppUrn != null ? cppUrn.hashCode() : 0);
        result = 31 * result + (defendants != null ? defendants.hashCode() : 0);
        result = 31 * result + (hearingType != null ? hearingType.hashCode() : 0);
        result = 31 * result + (judgeName != null ? judgeName.hashCode() : 0);
        result = 31 * result + (notBeforeTime != null ? notBeforeTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CaseDetail{" +
                "caseNumber='" + caseNumber + '\'' +
                ", caseType='" + caseType + '\'' +
                ", cppUrn='" + cppUrn + '\'' +
                ", defendants=" + defendants +
                ", hearingType='" + hearingType + '\'' +
                ", judgeName='" + judgeName + '\'' +
                ", notBeforeTime='" + notBeforeTime + '\'' +
                '}';
    }

    public CaseDetail setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
        return this;
    }

    public CaseDetail setCaseType(String caseType) {
        this.caseType = caseType;
        return this;
    }

    public CaseDetail setCppUrn(String cppUrn) {
        this.cppUrn = cppUrn;
        return this;
    }

    public CaseDetail setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
        return this;
    }

    public CaseDetail setHearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public static class Builder {
        private String caseNumber;

        private String caseType;

        private String cppUrn;

        private List<Defendant> defendants;

        private String hearingType;

        private String judgeName;

        private String notBeforeTime;

        private HearingEvent hearingEvent;

        public CaseDetail.Builder withCaseNumber(final String caseNumber) {
            this.caseNumber = caseNumber;
            return this;
        }

        public Builder withCaseType(final String caseType) {
            this.caseType = caseType;
            return this;
        }

        public CaseDetail.Builder withCppUrn(final String cppUrn) {
            this.cppUrn = cppUrn;
            return this;
        }

        public CaseDetail.Builder withDefendants(final List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

        public Builder withHearingType(final String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Builder withJudgeName(final String judgeName) {
            this.judgeName = judgeName;
            return this;
        }

        public Builder withNotBeforeTime(final String notBeforeTime) {
            this.notBeforeTime = notBeforeTime;
            return this;
        }

        public Builder withHearingEvent(final HearingEvent hearingEvent) {
            this.hearingEvent = hearingEvent;
            return this;
        }

        public CaseDetail build() {
            return new CaseDetail(caseNumber, caseType, cppUrn, defendants, hearingType, judgeName, notBeforeTime, hearingEvent);
        }
    }
}
