package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.respondent-counsel-change-ignored")
public class RespondentCounselChangeIgnored implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;
    private final String reason;

    @JsonCreator
    public RespondentCounselChangeIgnored(@JsonProperty("reason")final String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
