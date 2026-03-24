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
}
