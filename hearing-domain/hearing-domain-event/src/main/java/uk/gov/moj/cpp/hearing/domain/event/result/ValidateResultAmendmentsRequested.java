package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.validate-result-amendments-requested")
public class ValidateResultAmendmentsRequested implements Serializable {
    private static final long serialVersionUID = -5995314363348475391L;

    private final UUID hearingId;

    private final UUID userId;

    private final ZonedDateTime validateResultAmendmentsTime;

    @JsonCreator
    public ValidateResultAmendmentsRequested(@JsonProperty("hearingId") final UUID hearingId,
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

    public static ValidateResultAmendmentsRequested.ValidateResultAmendmentsRequestedBuilder validateResultAmendmentsRequested() {
        return new ValidateResultAmendmentsRequested.ValidateResultAmendmentsRequestedBuilder();
    }

    public static final class ValidateResultAmendmentsRequestedBuilder {
        private UUID hearingId;
        private UUID userId;
        private ZonedDateTime validateResultAmendmentsTime;

        private ValidateResultAmendmentsRequestedBuilder() {
        }

        public ValidateResultAmendmentsRequested.ValidateResultAmendmentsRequestedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ValidateResultAmendmentsRequested.ValidateResultAmendmentsRequestedBuilder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }

        public ValidateResultAmendmentsRequested.ValidateResultAmendmentsRequestedBuilder withValidateResultAmendmentsTime(final ZonedDateTime validateResultAmendmentsTime) {
            this.validateResultAmendmentsTime = validateResultAmendmentsTime;
            return this;
        }

        public ValidateResultAmendmentsRequested build() {
            return new ValidateResultAmendmentsRequested(hearingId, userId, validateResultAmendmentsTime);
        }
    }
}
