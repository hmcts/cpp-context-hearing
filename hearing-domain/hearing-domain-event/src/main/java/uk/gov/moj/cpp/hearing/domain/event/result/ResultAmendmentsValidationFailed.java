package uk.gov.moj.cpp.hearing.domain.event.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.event.result-amendments-validation-failed")
public class ResultAmendmentsValidationFailed implements Serializable {
    private static final long serialVersionUID = -5995314363348475391L;

    private final UUID hearingId;

    private final UUID userId;

    private final ZonedDateTime validateResultAmendmentsTime;

    @JsonCreator
    public ResultAmendmentsValidationFailed(@JsonProperty("hearingId") final UUID hearingId,
                                            @JsonProperty("userId") final UUID userId,
                                            @JsonProperty("validateResultAmendmentsTime") final ZonedDateTime validateResultAmendmentsTime) {

        this.hearingId = hearingId;
        this.userId = userId;
        this.validateResultAmendmentsTime = validateResultAmendmentsTime;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public ZonedDateTime getValidateResultAmendmentsTime() {
        return this.validateResultAmendmentsTime;
    }

}
