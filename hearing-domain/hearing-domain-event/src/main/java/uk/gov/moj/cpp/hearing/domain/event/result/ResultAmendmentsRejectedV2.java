package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

@Event("hearing.event.result-amendments-rejected-v2")
public class ResultAmendmentsRejectedV2 implements Serializable {
    private static final long serialVersionUID = -5995314363348475391L;

    private final UUID hearingId;
    private final UUID userId;
    private final ZonedDateTime validateResultAmendmentsTime;
    private final List<Target2> latestSharedTargets;
    private final ZonedDateTime lastSharedDateTime;

    private final LocalDate hearingDay;

    @JsonCreator
    public ResultAmendmentsRejectedV2(final UUID hearingId,
                                      final LocalDate hearingDay,
                                      final UUID userId,
                                      final ZonedDateTime validateResultAmendmentsTime,
                                      final List<Target2> latestSharedTargets,
                                      final ZonedDateTime lastSharedDateTime) {

        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
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

    public List<Target2> getLatestSharedTargets() {
        return new ArrayList<>(latestSharedTargets);
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public static ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder resultAmendmentsRejected() {
        return new ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder();
    }

    public static final class ResultAmendmentsRejectedBuilder {
        private UUID hearingId;
        private LocalDate hearingDay;
        private UUID userId;
        private ZonedDateTime validateResultAmendmentsTime;
        private  List<Target2> latestSharedTargets;
        private  ZonedDateTime lastSharedDateTime;
        private ResultAmendmentsRejectedBuilder() {
        }

        public UUID getHearingId() {
            return hearingId;
        }

        public void setHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
        }

        public LocalDate getHearingDay() {
            return hearingDay;
        }

        public void setHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
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

        public List<Target2> getLatestSharedTargets() {
            return new ArrayList<>(latestSharedTargets);
        }

        public void setLatestSharedTargets(final List<Target2> latestSharedTargets) {
            this.latestSharedTargets = new ArrayList<>(latestSharedTargets);
        }

        public ZonedDateTime getLastSharedDateTime() {
            return lastSharedDateTime;
        }

        public void setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
        }

        public ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }
        public ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder withLatestSharedTargets(final List<Target2> latestSharedTargets) {
            this.latestSharedTargets = new ArrayList<>(latestSharedTargets);
            return this;
        }
        public ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder withLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public ResultAmendmentsRejectedV2.ResultAmendmentsRejectedBuilder withValidateResultAmendmentsTime(final ZonedDateTime validateResultAmendmentsTime) {
            this.validateResultAmendmentsTime = validateResultAmendmentsTime;
            return this;
        }

        public ResultAmendmentsRejectedV2 build() {
            return new ResultAmendmentsRejectedV2(hearingId, hearingDay, userId, validateResultAmendmentsTime, latestSharedTargets, lastSharedDateTime);
        }
    }
}
