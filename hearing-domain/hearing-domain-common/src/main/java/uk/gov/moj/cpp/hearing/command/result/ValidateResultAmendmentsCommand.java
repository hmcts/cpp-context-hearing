package uk.gov.moj.cpp.hearing.command.result;

import java.time.LocalDate;
import java.util.UUID;

public class ValidateResultAmendmentsCommand {

    private UUID id;
    private UUID userId;
    private String validateAction;

    private LocalDate hearingDay;

    public ValidateResultAmendmentsCommand() {
    }

    private ValidateResultAmendmentsCommand(final UUID id,
                                            final UUID userId,
                                            final String validateAction,
                                            final LocalDate hearingDay) {
        this.id = id;
        this.userId = userId;
        this.validateAction = validateAction;
        this.hearingDay = hearingDay;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final ValidateResultAmendmentsCommand copy) {
        final Builder builder = new Builder();
        builder.id = copy.getId();
        builder.userId = copy.getUserId();
        builder.hearingDay = copy.getHearingDay();
        return builder;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public String getValidateAction() {
        return validateAction;
    }

    public static final class Builder {
        private UUID id;
        private UUID userId;
        private String validateAction;
        private LocalDate hearingDay;

        private Builder() {
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder withValidateAction(final String validateAction) {
            this.validateAction = validateAction;
            return this;
        }

        public Builder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public ValidateResultAmendmentsCommand build() {
            return new ValidateResultAmendmentsCommand(id, userId, validateAction, hearingDay);
        }
    }
}
