package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"squid:S1067"})
public class CaseDetail implements Serializable {
    private static final long serialVersionUID = -4951785613141170679L;

    private String caseNumber;

    private String caseType;

    private String cppUrn;

    private List<Defendant> defendants;

    private String hearingType;

    private String judgeName;

    public CaseDetail(final String caseNumber, final String caseType, final String cppUrn, final List<Defendant> defendants, final String hearingType, final String judgeName) {
        this.caseNumber = caseNumber;
        this.caseType = caseType;
        this.cppUrn = cppUrn;
        this.defendants = defendants;
        this.hearingType = hearingType;
        this.judgeName = judgeName;
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

    public static Builder caseDetail() {
        return new CaseDetail.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        final CaseDetail that = (CaseDetail) obj;

        return java.util.Objects.equals(this.caseNumber, that.caseNumber) &&
                java.util.Objects.equals(this.caseType, that.caseType) &&
                java.util.Objects.equals(this.cppUrn, that.cppUrn) &&
                java.util.Objects.equals(this.defendants, that.defendants) &&
                java.util.Objects.equals(this.judgeName, that.judgeName) &&
                java.util.Objects.equals(this.hearingType, that.hearingType);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(caseNumber, caseType, cppUrn, defendants, hearingType);
    }

    @Override
    public String toString() {
        return "CaseDetail{" +
                "caseNumber='" + caseNumber + "'," +
                "caseType='" + caseType + "'," +
                "cppUrn='" + cppUrn + "'," +
                "defendants='" + defendants + "'," +
                "judgeName='" + judgeName + "'," +
                "hearingType='" + hearingType + "'" +
                "}";
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


        public CaseDetail build() {
            return new CaseDetail(caseNumber, caseType, cppUrn, defendants, hearingType, judgeName);
        }
    }
}
