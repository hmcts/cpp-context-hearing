package uk.gov.moj.cpp.hearing.command.result;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ValidateResultAmendmentsCommand {

    private UUID id;
    private UUID userId;
    private ZonedDateTime validateAmendmentsTime;

    public ValidateResultAmendmentsCommand() {
    }

    private ValidateResultAmendmentsCommand(final UUID id,
                                            final UUID userId,
                                            final ZonedDateTime validateAmendmentsTime) {
        this.id = id;
        this.userId = userId;
        this.validateAmendmentsTime = validateAmendmentsTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final ValidateResultAmendmentsCommand copy) {
        final Builder builder = new Builder();
        builder.id = copy.getId();
        builder.userId = copy.getUserId();
        builder.validateAmendmentsTime = copy.getValidateAmendmentsTime();
        return builder;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public ZonedDateTime getValidateAmendmentsTime() {
        return validateAmendmentsTime;
    }

    public static final class Builder {
        private UUID id;
        private UUID userId;
        private ZonedDateTime validateAmendmentsTime;

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

        public Builder withValidateAmendmentsTime(final ZonedDateTime validateAmendmentsTime) {
            this.validateAmendmentsTime = validateAmendmentsTime;
            return this;
        }

        public ValidateResultAmendmentsCommand build() {
            return new ValidateResultAmendmentsCommand(id, userId, validateAmendmentsTime);
        }
    }
}
