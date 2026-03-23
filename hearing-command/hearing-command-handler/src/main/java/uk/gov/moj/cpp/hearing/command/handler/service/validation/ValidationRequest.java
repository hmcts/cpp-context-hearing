package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationRequest {

    private final String hearingId;
    private final LocalDate hearingDay;
    private final String courtType;
    private final String caseId;
    private final List<ResultLineDto> resultLines;
    private final List<OffenceDto> offences;
    private final List<DefendantDto> defendants;

    public ValidationRequest(
            final String hearingId,
            final LocalDate hearingDay,
            final String courtType,
            final String caseId,
            final List<ResultLineDto> resultLines,
            final List<OffenceDto> offences,
            final List<DefendantDto> defendants) {
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.courtType = courtType;
        this.caseId = caseId;
        this.resultLines = resultLines;
        this.offences = offences;
        this.defendants = defendants;
    }

    public String getHearingId() {
        return hearingId;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public String getCourtType() {
        return courtType;
    }

    public String getCaseId() {
        return caseId;
    }

    public List<ResultLineDto> getResultLines() {
        return resultLines;
    }

    public List<OffenceDto> getOffences() {
        return offences;
    }

    public List<DefendantDto> getDefendants() {
        return defendants;
    }

    public static class ResultLineDto {

        private final String id;
        private final String shortCode;
        private final String label;
        private final String defendantId;
        private final String offenceId;
        @JsonProperty("isConcurrent")
        private final Boolean isConcurrent;
        private final String consecutiveToOffence;

        public ResultLineDto(final String id, final String shortCode, final String label,
                             final String defendantId, final String offenceId,
                             final Boolean isConcurrent, final String consecutiveToOffence) {
            this.id = id;
            this.shortCode = shortCode;
            this.label = label;
            this.defendantId = defendantId;
            this.offenceId = offenceId;
            this.isConcurrent = isConcurrent;
            this.consecutiveToOffence = consecutiveToOffence;
        }

        public String getId() {
            return id;
        }

        public String getShortCode() {
            return shortCode;
        }

        public String getLabel() {
            return label;
        }

        public String getDefendantId() {
            return defendantId;
        }

        public String getOffenceId() {
            return offenceId;
        }

        public Boolean getIsConcurrent() {
            return isConcurrent;
        }

        public String getConsecutiveToOffence() {
            return consecutiveToOffence;
        }
    }

    public static class OffenceDto {

        private final String id;
        private final String offenceCode;
        private final String offenceTitle;
        private final Integer orderIndex;
        private final String caseUrn;

        public OffenceDto(final String id, final String offenceCode,
                          final String offenceTitle, final Integer orderIndex,
                          final String caseUrn) {
            this.id = id;
            this.offenceCode = offenceCode;
            this.offenceTitle = offenceTitle;
            this.orderIndex = orderIndex;
            this.caseUrn = caseUrn;
        }

        public String getId() {
            return id;
        }

        public String getOffenceCode() {
            return offenceCode;
        }

        public String getOffenceTitle() {
            return offenceTitle;
        }

        public Integer getOrderIndex() {
            return orderIndex;
        }

        public String getCaseUrn() {
            return caseUrn;
        }
    }

    public static class DefendantDto {

        private final String id;
        private final String firstName;
        private final String lastName;
        private final String masterDefendantId;

        public DefendantDto(final String id, final String firstName, final String lastName,
                            final String masterDefendantId) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.masterDefendantId = masterDefendantId;
        }

        public String getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getMasterDefendantId() {
            return masterDefendantId;
        }
    }
}
