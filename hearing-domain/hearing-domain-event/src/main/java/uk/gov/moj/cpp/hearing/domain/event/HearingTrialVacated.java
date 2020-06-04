package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.trial-vacated")
public class HearingTrialVacated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID vacatedTrialReasonId;
    private String code;
    private String description;
    private String type;

    @JsonCreator
    public HearingTrialVacated(@JsonProperty("hearingId") final UUID hearingId,
                               @JsonProperty("vacatedTrialReasonId") final UUID vacatedTrialReasonId,
                               @JsonProperty("code") final String code,
                               @JsonProperty("type") final String type,
                               @JsonProperty("description") final String description) {
        this.hearingId = hearingId;
        this.vacatedTrialReasonId = vacatedTrialReasonId;
        this.code = code;
        this.description = description;
        this.type = type;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public void setVacatedTrialReasonId(final UUID vacatedTrialReasonId) {
        this.vacatedTrialReasonId = vacatedTrialReasonId;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
