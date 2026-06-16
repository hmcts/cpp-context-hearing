package uk.gov.moj.cpp.hearing.command.handler.service.validation;
public class OffenceDto {

    private final String offenceId;
    private final String offenceCode;
    private final String offenceTitle;
    private final Integer orderIndex;
    private final String caseUrn;

    private OffenceDto(Builder builder) {
        this.offenceId = builder.offenceId;
        this.offenceCode = builder.offenceCode;
        this.offenceTitle = builder.offenceTitle;
        this.orderIndex = builder.orderIndex;
        this.caseUrn = builder.caseUrn;
    }

    // Getters
    public String getOffenceId() { return offenceId; }
    public String getOffenceCode() { return offenceCode; }
    public String getOffenceTitle() { return offenceTitle; }
    public Integer getOrderIndex() { return orderIndex; }
    public String getCaseUrn() { return caseUrn; }

    // Builder
    public static class Builder {
        private String offenceId;
        private String offenceCode;
        private String offenceTitle;
        private Integer orderIndex;
        private String caseUrn;

        public Builder offenceId(String offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder offenceCode(String offenceCode) {
            this.offenceCode = offenceCode;
            return this;
        }

        public Builder offenceTitle(String offenceTitle) {
            this.offenceTitle = offenceTitle;
            return this;
        }

        public Builder orderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
            return this;
        }

        public Builder caseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public OffenceDto build() {
            return new OffenceDto(this);
        }
    }

    // Optional convenience method
    public static Builder builder() {
        return new Builder();
    }
}
