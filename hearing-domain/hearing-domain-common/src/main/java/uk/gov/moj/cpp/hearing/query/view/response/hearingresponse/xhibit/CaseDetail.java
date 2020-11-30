package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.HearingEvent;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"squid:S1067"})
public class CaseDetail implements Serializable {
    private static final long serialVersionUID = -4951785613141170679L;

    private BigInteger hearingprogress;

    private BigInteger activeCase;

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

    private PublicNotices publicNotices;

    public CaseDetail(final String caseNumber, final String caseType, final String cppUrn, final List<Defendant> defendants, final String hearingType, final String judgeName, final String notBeforeTime,
                      final HearingEvent hearingEvent,
                      final BigInteger activecase,
                      final BigInteger hearingprogress,
                      final PublicNotices publicNotices) {
        this.caseNumber = caseNumber;
        this.caseType = caseType;
        this.cppUrn = cppUrn;
        this.defendants = defendants;
        this.hearingType = hearingType;
        this.judgeName = judgeName;
        this.notBeforeTime = notBeforeTime;
        this.hearingEvent = hearingEvent;
        this.activeCase = activecase;
        this.hearingprogress = hearingprogress;
        this.publicNotices = publicNotices;
    }

    public BigInteger getActivecase() {
        return activeCase;
    }

    public BigInteger getHearingprogress() {
        return hearingprogress;
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

    public PublicNotices getPublicNotices() { return publicNotices; }


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
        if (!(o instanceof CaseDetail)) return false;

        CaseDetail that = (CaseDetail) o;

        if (getHearingprogress() != null ? !getHearingprogress().equals(that.getHearingprogress()) : that.getHearingprogress() != null)
            return false;
        if (activeCase != null ? !activeCase.equals(that.activeCase) : that.activeCase != null)
            return false;
        if (getCaseNumber() != null ? !getCaseNumber().equals(that.getCaseNumber()) : that.getCaseNumber() != null)
            return false;
        if (getCaseType() != null ? !getCaseType().equals(that.getCaseType()) : that.getCaseType() != null)
            return false;
        if (getCppUrn() != null ? !getCppUrn().equals(that.getCppUrn()) : that.getCppUrn() != null)
            return false;
        if (getDefendants() != null ? !getDefendants().equals(that.getDefendants()) : that.getDefendants() != null)
            return false;
        if (getHearingType() != null ? !getHearingType().equals(that.getHearingType()) : that.getHearingType() != null)
            return false;
        if (getJudgeName() != null ? !getJudgeName().equals(that.getJudgeName()) : that.getJudgeName() != null)
            return false;
        if (getNotBeforeTime() != null ? !getNotBeforeTime().equals(that.getNotBeforeTime()) : that.getNotBeforeTime() != null)
            return false;
        if (getHearingEvent() != null ? !getHearingEvent().equals(that.getHearingEvent()) : that.getHearingEvent() != null)
            return false;
        if (getLinkedCaseIds() != null ? !getLinkedCaseIds().equals(that.getLinkedCaseIds()) : that.getLinkedCaseIds() != null)
            return false;
        if (getPublicNotices() != null ? !getPublicNotices().equals(that.getPublicNotices()) : that.getPublicNotices() != null)
            return false;
        return defenceCounsels != null ? defenceCounsels.equals(that.defenceCounsels) : that.defenceCounsels == null;
    }

    @SuppressWarnings({"squid:S3776", "squid:S00121", "squid:S00122"} )
    @Override
    public int hashCode() {
        int result = getHearingprogress() != null ? getHearingprogress().hashCode() : 0;
        result = 31 * result + (activeCase != null ? activeCase.hashCode() : 0);
        result = 31 * result + (getCaseNumber() != null ? getCaseNumber().hashCode() : 0);
        result = 31 * result + (getCaseType() != null ? getCaseType().hashCode() : 0);
        result = 31 * result + (getCppUrn() != null ? getCppUrn().hashCode() : 0);
        result = 31 * result + (getDefendants() != null ? getDefendants().hashCode() : 0);
        result = 31 * result + (getHearingType() != null ? getHearingType().hashCode() : 0);
        result = 31 * result + (getJudgeName() != null ? getJudgeName().hashCode() : 0);
        result = 31 * result + (getNotBeforeTime() != null ? getNotBeforeTime().hashCode() : 0);
        result = 31 * result + (getHearingEvent() != null ? getHearingEvent().hashCode() : 0);
        result = 31 * result + (getLinkedCaseIds() != null ? getLinkedCaseIds().hashCode() : 0);
        result = 31 * result + (getPublicNotices() != null ? getPublicNotices().hashCode() : 0);
        result = 31 * result + (defenceCounsels != null ? defenceCounsels.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CaseDetail{" +
                "hearingprogress=" + hearingprogress +
                ", activeCase=" + activeCase +
                ", caseNumber='" + caseNumber + '\'' +
                ", caseType='" + caseType + '\'' +
                ", cppUrn='" + cppUrn + '\'' +
                ", defendants=" + defendants +
                ", hearingType='" + hearingType + '\'' +
                ", judgeName='" + judgeName + '\'' +
                ", notBeforeTime='" + notBeforeTime + '\'' +
                ", hearingEvent=" + hearingEvent +
                ", linkedCaseIds=" + linkedCaseIds +
                ", defenceCounsels=" + defenceCounsels +
                ", publicNotices=" + publicNotices +
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

        private BigInteger hearingprogress;

        private BigInteger activecase;

        private String caseNumber;

        private String caseType;

        private String cppUrn;

        private List<Defendant> defendants;

        private String hearingType;

        private String judgeName;

        private String notBeforeTime;

        private HearingEvent hearingEvent;

        private PublicNotices publicNotices;

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

        public Builder withActivecase(final BigInteger activecase) {
            this.activecase = activecase;
            return this;
        }

        public Builder withHearingprogress(final BigInteger hearingprogress) {
            this.hearingprogress = hearingprogress;
            return this;
        }

        public Builder withPublicNotices(final PublicNotices publicNotices) {
            this.publicNotices = publicNotices;
            return this;
        }


        public CaseDetail build() {
            return new CaseDetail(caseNumber, caseType, cppUrn, defendants, hearingType, judgeName, notBeforeTime, hearingEvent, activecase, hearingprogress,publicNotices);
        }
    }
}
