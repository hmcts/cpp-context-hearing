package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantCase implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID caseId;
    private String bailStatus;
    private LocalDate custodyTimeLimitDate;

    public DefendantCase() {
    }

    @JsonCreator
    public DefendantCase(@JsonProperty("caseId") final UUID caseId,
                         @JsonProperty("bailStatus") final String bailStatus,
                         @JsonProperty("custodyTimeLimitDate") final LocalDate custodyTimeLimitDate
    ) {

        this.caseId = caseId;
        this.bailStatus = bailStatus;
        this.custodyTimeLimitDate = custodyTimeLimitDate;
    }

    public static DefendantCase defendantCase() {
        return new DefendantCase();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public DefendantCase setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public DefendantCase setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
        return this;
    }

    public LocalDate getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public DefendantCase setCustodyTimeLimitDate(LocalDate custodyTimeLimitDate) {
        this.custodyTimeLimitDate = custodyTimeLimitDate;
        return this;
    }
}