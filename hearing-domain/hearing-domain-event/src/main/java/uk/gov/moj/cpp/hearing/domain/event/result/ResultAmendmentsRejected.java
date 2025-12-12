package uk.gov.moj.cpp.hearing.domain.event.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.event.result-amendments-rejected")
public class ResultAmendmentsRejected implements Serializable {
    private static final long serialVersionUID = -5995314363348475391L;

    private final UUID hearingId;
    private final UUID userId;
    private final ZonedDateTime validateResultAmendmentsTime;
    private final List<Target> latestSharedTargets;
    private final ZonedDateTime lastSharedDateTime;

    @JsonCreator
    public ResultAmendmentsRejected( final UUID hearingId,
                                     final UUID userId,
                                     final ZonedDateTime validateResultAmendmentsTime,
                                     final List<Target> latestSharedTargets,
                                     final ZonedDateTime lastSharedDateTime) {

        this.hearingId = hearingId;
        this.userId = userId;
        this.validateResultAmendmentsTime = validateResultAmendmentsTime;
        this.latestSharedTargets = new ArrayList<>(latestSharedTargets);
        this.lastSharedDateTime = lastSharedDateTime;
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

    public List<Target> getLatestSharedTargets() {

        return new ArrayList<>(latestSharedTargets);
    }

    public ZonedDateTime getLastSharedDateTime() {

        return lastSharedDateTime;
    }

    public static ResultAmendmentsRejected.ResultAmendmentsRejectedBuilder resultAmendmentsRejected() {
        return new ResultAmendmentsRejected.ResultAmendmentsRejectedBuilder();
    }

    public static final class ResultAmendmentsRejectedBuilder {
        private UUID hearingId;
        private UUID userId;
        private ZonedDateTime validateResultAmendmentsTime;
        private  List<Target> latestSharedTargets;
        private  ZonedDateTime lastSharedDateTime;
        private ResultAmendmentsRejectedBuilder() {
        }

        public UUID getHearingId() {
            return hearingId;
        }

        public void setHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(final UUID userId) {
            this.userId = userId;
        }

        public ZonedDateTime getValidateResultAmendmentsTime() {
            return validateResultAmendmentsTime;
        }

        public void setValidateResultAmendmentsTime(final ZonedDateTime validateResultAmendmentsTime) {
            this.validateResultAmendmentsTime = validateResultAmendmentsTime;
        }

        public List<Target> getLatestSharedTargets() {
            return new ArrayList<>(latestSharedTargets);
        }

        public void setLatestSharedTargets(final List<Target> latestSharedTargets) {
            this.latestSharedTargets = new ArrayList<>(latestSharedTargets);
        }

        public ZonedDateTime getLastSharedDateTime() {
            return lastSharedDateTime;
        }

        public void setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
        }

        public ResultAmendmentsRejected.ResultAmendmentsRejectedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ResultAmendmentsRejected.ResultAmendmentsRejectedBuilder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }
        public ResultAmendmentsRejected.ResultAmendmentsRejectedBuilder withLatestSharedTargets(final List<Target> latestSharedTargets) {
            this.latestSharedTargets = new ArrayList<>(latestSharedTargets);
            return this;
        }
        public ResultAmendmentsRejected.ResultAmendmentsRejectedBuilder withLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public ResultAmendmentsRejected.ResultAmendmentsRejectedBuilder withValidateResultAmendmentsTime(final ZonedDateTime validateResultAmendmentsTime) {
            this.validateResultAmendmentsTime = validateResultAmendmentsTime;
            return this;
        }

        public ResultAmendmentsRejected build() {
            return new ResultAmendmentsRejected(hearingId, userId, validateResultAmendmentsTime, latestSharedTargets, lastSharedDateTime);
        }
    }
}
