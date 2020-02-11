package uk.gov.moj.cpp.hearing.nces;

import uk.gov.justice.core.courts.nowdocument.DefendantCaseOffence;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"squid:S00107"})
public class DocumentContent implements Serializable {
    private static final long serialVersionUID = -4485480031610795301L;

    private final String adjustmentDetails;

    private final LocalDate amendmentDate;

    private String amendmentType;

    private final String courtCentreName;

    private final uk.gov.moj.cpp.hearing.nces.Defendant defendant;

    private final List<DefendantCaseOffence> defendantCaseOffences;

    private final String divisionCode;

    private final String gobAccountNumber;

    private final String oldDivisionCode;

    private final String oldGobAccountNumber;

    private final String urn;

    public DocumentContent(final String adjustmentDetails, final LocalDate amendmentDate, final String amendmentType, final String courtCentreName, final uk.gov.moj.cpp.hearing.nces.Defendant defendant, final List<DefendantCaseOffence> defendantCaseOffences, final String divisionCode, final String gobAccountNumber, final String oldDivisionCode, final String oldGobAccountNumber, final String urn) {
        this.adjustmentDetails = adjustmentDetails;
        this.amendmentDate = amendmentDate;
        this.amendmentType = amendmentType;
        this.courtCentreName = courtCentreName;
        this.defendant = defendant;
        this.defendantCaseOffences = defendantCaseOffences;
        this.divisionCode = divisionCode;
        this.gobAccountNumber = gobAccountNumber;
        this.oldDivisionCode = oldDivisionCode;
        this.oldGobAccountNumber = oldGobAccountNumber;
        this.urn = urn;
    }

    public String getAdjustmentDetails() {
        return adjustmentDetails;
    }

    public LocalDate getAmendmentDate() {
        return amendmentDate;
    }

    public String getAmendmentType() {
        return amendmentType;
    }

    public void setAmendmentType(String amendmentType) {
        this.amendmentType = amendmentType;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public uk.gov.moj.cpp.hearing.nces.Defendant getDefendant() {
        return defendant;
    }

    public List<DefendantCaseOffence> getDefendantCaseOffences() {
        return defendantCaseOffences;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public String getGobAccountNumber() {
        return gobAccountNumber;
    }

    public String getOldDivisionCode() {
        return oldDivisionCode;
    }

    public String getOldGobAccountNumber() {
        return oldGobAccountNumber;
    }

    public String getUrn() {
        return urn;
    }

    public static Builder documentContent() {
        return new DocumentContent.Builder();
    }

    public static Builder newBuilderFrom(DocumentContent documentContent) {
        Builder builder = new Builder();
        if (documentContent != null) {
            builder.withAdjustmentDetails(documentContent.getAdjustmentDetails())
                    .withAmendmentDate(documentContent.getAmendmentDate())
                    .withAmendmentType(documentContent.getAmendmentType())
                    .withCourtCentreName(documentContent.getCourtCentreName())
                    .withDefendant(documentContent.getDefendant())
                    .withDefendantCaseOffences(documentContent.getDefendantCaseOffences())
                    .withDivisionCode(documentContent.getDivisionCode())
                    .withGobAccountNumber(documentContent.getGobAccountNumber())
                    .withOldDivisionCode(documentContent.getOldDivisionCode())
                    .withOldGobAccountNumber(documentContent.getOldGobAccountNumber())
                    .withUrn(documentContent.getUrn());
        }
        return builder;
    }

    @Override
    @SuppressWarnings({"squid:S1067", "squid:S00121"})
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentContent)) return false;
        final DocumentContent that = (DocumentContent) o;
        return Objects.equals(adjustmentDetails, that.adjustmentDetails) &&
                Objects.equals(amendmentDate, that.amendmentDate) &&
                Objects.equals(amendmentType, that.amendmentType) &&
                Objects.equals(courtCentreName, that.courtCentreName) &&
                Objects.equals(defendant, that.defendant) &&
                Objects.equals(defendantCaseOffences, that.defendantCaseOffences) &&
                Objects.equals(divisionCode, that.divisionCode) &&
                Objects.equals(gobAccountNumber, that.gobAccountNumber) &&
                Objects.equals(oldDivisionCode, that.oldDivisionCode) &&
                Objects.equals(oldGobAccountNumber, that.oldGobAccountNumber) &&
                Objects.equals(urn, that.urn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adjustmentDetails, amendmentDate, amendmentType, courtCentreName, defendant, defendantCaseOffences, divisionCode, gobAccountNumber, oldDivisionCode, oldGobAccountNumber, urn);
    }

    public static class Builder {
        private String adjustmentDetails;

        private LocalDate amendmentDate;

        private String amendmentType;

        private String courtCentreName;

        private Defendant defendant;

        private List<DefendantCaseOffence> defendantCaseOffences;

        private String divisionCode;

        private String gobAccountNumber;

        private String oldDivisionCode;

        private String oldGobAccountNumber;

        private String urn;

        public Builder withAdjustmentDetails(final String adjustmentDetails) {
            this.adjustmentDetails = adjustmentDetails;
            return this;
        }

        public Builder withAmendmentDate(final LocalDate amendmentDate) {
            this.amendmentDate = amendmentDate;
            return this;
        }

        public Builder withAmendmentType(final String amendmentType) {
            this.amendmentType = amendmentType;
            return this;
        }

        public Builder withCourtCentreName(final String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withDefendant(final Defendant defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withDefendantCaseOffences(final List<DefendantCaseOffence> defendantCaseOffences) {
            this.defendantCaseOffences = defendantCaseOffences;
            return this;
        }

        public Builder withDivisionCode(final String divisionCode) {
            this.divisionCode = divisionCode;
            return this;
        }

        public Builder withGobAccountNumber(final String gobAccountNumber) {
            this.gobAccountNumber = gobAccountNumber;
            return this;
        }

        public Builder withOldDivisionCode(final String oldDivisionCode) {
            this.oldDivisionCode = oldDivisionCode;
            return this;
        }

        public Builder withOldGobAccountNumber(final String oldGobAccountNumber) {
            this.oldGobAccountNumber = oldGobAccountNumber;
            return this;
        }

        public Builder withUrn(final String urn) {
            this.urn = urn;
            return this;
        }

        public DocumentContent build() {
            return new DocumentContent(adjustmentDetails, amendmentDate, amendmentType, courtCentreName, defendant, defendantCaseOffences, divisionCode, gobAccountNumber, oldDivisionCode, oldGobAccountNumber, urn);
        }
    }
}
