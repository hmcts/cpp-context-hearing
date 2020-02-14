package uk.gov.moj.cpp.hearing.nces;

import uk.gov.justice.core.courts.notification.EmailChannel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings({"squid:S1948","squid:S00107"})
public class FinancialOrderForDefendant implements Serializable {
    private static final long serialVersionUID = -4485480031610795301L;

    private final UUID caseId;

    private final UUID defendantId;

    private final List<UUID> resultDefinitionIds;


    private final DocumentContent documentContent;

    private final List<EmailChannel> emailNotifications;

    private final UUID hearingId;

    private final UUID materialId;

    private final Map<String, Object> additionalProperties;

    public FinancialOrderForDefendant(final UUID caseId, final UUID defendantId, final List<UUID> resultDefinitionIds, final DocumentContent documentContent, final List<EmailChannel> emailNotifications, final UUID hearingId, final UUID materialId, final Map<String, Object> additionalProperties) {
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.resultDefinitionIds = resultDefinitionIds;
        this.documentContent = documentContent;
        this.emailNotifications = emailNotifications;
        this.hearingId = hearingId;
        this.materialId = materialId;
        this.additionalProperties = additionalProperties;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public List<UUID> getResultDefinitionIds() {
        return resultDefinitionIds;
    }



    public DocumentContent getDocumentContent() {
        return documentContent;
    }

    public List<EmailChannel> getEmailNotifications() {
        return emailNotifications;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public static Builder newBuilder() {
        return new FinancialOrderForDefendant.Builder();
    }

    public static Builder newBuilderFrom(FinancialOrderForDefendant financialOrderForDefendant) {
        return new FinancialOrderForDefendant.Builder()
                .withCaseId(financialOrderForDefendant.getCaseId())
                .withDefendantId(financialOrderForDefendant.getDefendantId())
                .withResultDefinitionIds(financialOrderForDefendant.getResultDefinitionIds())
                .withDocumentContent(financialOrderForDefendant.getDocumentContent())
                .withEmailNotifications(financialOrderForDefendant.getEmailNotifications())
                .withHearingId(financialOrderForDefendant.getHearingId())
                .withMaterialId(financialOrderForDefendant.getMaterialId())
                .withAdditionalProperties(financialOrderForDefendant.getAdditionalProperties());
    }

    @Override
    @SuppressWarnings({"squid:S1067", "squid:S00121"})
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinancialOrderForDefendant)) {
            return false;
        }
        final FinancialOrderForDefendant that = (FinancialOrderForDefendant) o;
        return
                Objects.equals(caseId, that.caseId) &&
                Objects.equals(defendantId, that.defendantId) &&
                Objects.equals(resultDefinitionIds, that.resultDefinitionIds) &&
                Objects.equals(documentContent, that.documentContent) &&
                Objects.equals(emailNotifications, that.emailNotifications) &&
                Objects.equals(hearingId, that.hearingId) &&
                Objects.equals(materialId, that.materialId) &&
                Objects.equals(additionalProperties, that.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId, defendantId, resultDefinitionIds,  documentContent, emailNotifications, hearingId, materialId, additionalProperties);
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public static class Builder {
        private UUID caseId;

        private UUID defendantId;

        private List<UUID> resultDefinitionIds;


        private DocumentContent documentContent;

        private List<EmailChannel> emailNotifications;

        private UUID hearingId;

        private UUID materialId;

        private Map<String, Object> additionalProperties = new HashMap<>();

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withResultDefinitionIds(final List<UUID> resultDefinitionIds) {
            this.resultDefinitionIds = resultDefinitionIds;
            return this;
        }


        public Builder withDocumentContent(final DocumentContent documentContent) {
            this.documentContent = documentContent;
            return this;
        }

        public Builder withEmailNotifications(final List<EmailChannel> emailNotifications) {
            this.emailNotifications = emailNotifications;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withMaterialId(final UUID materialId) {
            this.materialId = materialId;
            return this;
        }

        public Builder withAdditionalProperty(final String name, final Object value) {
            additionalProperties.put(name, value);
            return this;
        }

        public Builder withAdditionalProperties(Map<String, Object> additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }

        public FinancialOrderForDefendant build() {
            return new FinancialOrderForDefendant(caseId, defendantId, resultDefinitionIds, documentContent, emailNotifications, hearingId, materialId, additionalProperties);
        }
    }
}
