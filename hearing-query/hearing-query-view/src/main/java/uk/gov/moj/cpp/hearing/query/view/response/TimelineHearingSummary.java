package uk.gov.moj.cpp.hearing.query.view.response;

import static java.time.format.DateTimeFormatter.ofPattern;

import uk.gov.justice.core.courts.YouthCourt;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TimelineHearingSummary {

    private static final DateTimeFormatter DATE_FORMATTER = ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = ofPattern("HH:mm");

    private final UUID hearingId;
    private final LocalDate hearingDate;
    private final String hearingType;
    private final String courtHouse;
    private final String courtRoom;
    private final ZonedDateTime hearingTime;
    private final ZonedDateTime startTime;
    private final Integer estimatedDuration;
    private final String outcome;
    private final List<String> defendants;
    private final List<String> applicants;
    private List<String> youthDefendantIds;
    private final YouthCourt youthCourt;
    private final Boolean isBoxHearing;

    public TimelineHearingSummary(final TimelineHearingSummaryBuilder builder) {
        this.hearingId = builder.hearingId;
        this.hearingDate = builder.hearingDate;
        this.hearingType = builder.hearingType;
        this.courtHouse = builder.courtHouse;
        this.courtRoom = builder.courtRoom;
        this.hearingTime = builder.hearingTime;
        this.startTime = builder.startTime;
        this.estimatedDuration = builder.estimatedDuration;
        this.defendants = builder.defendants;
        this.applicants = builder.applicants;
        this.outcome = builder.outcome;
        this.youthDefendantIds = builder.youthDefendantIds;
        this.youthCourt = builder.youthCourt;
        this.isBoxHearing = builder.isBoxHearing;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    @JsonIgnore
    public LocalDate getHearingDate() {
        return hearingDate;
    }

    @JsonProperty("hearingDate")
    public String getHearingDateAsString() {
        return hearingDate.format(DATE_FORMATTER);
    }

    public String getHearingType() {
        return hearingType;
    }

    public List<String> getYouthDefendantIds() {
        return new ArrayList<>(youthDefendantIds);
    }

    public void setYouthDefendantIds(final List<String> youthDefendantIds) {
        this.youthDefendantIds = new ArrayList<>(youthDefendantIds);
    }

    public YouthCourt getYouthCourt() {
        return youthCourt;
    }

    public String getCourtHouse() {
        return courtHouse;
    }

    public String getCourtRoom() {
        return courtRoom;
    }

    public String getHearingTime() {
        return hearingTime.format(TIME_FORMATTER);
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public List<String> getDefendants() {
        return defendants;
    }

    public List<String> getApplicants() { return applicants; }

    public String getOutcome() {
        return outcome;
    }

    public Boolean getIsBoxHearing(){ return isBoxHearing;}

    public static class TimelineHearingSummaryBuilder {
        private UUID hearingId;
        private LocalDate hearingDate;
        private String hearingType;
        private String courtHouse;
        private String courtRoom;
        private ZonedDateTime hearingTime;
        private ZonedDateTime startTime;
        private Integer estimatedDuration;
        private List<String> defendants;
        private List<String> applicants;
        private String outcome;
        private List<String> youthDefendantIds;
        private YouthCourt youthCourt;

        public List<String> getYouthDefendantIds() {
            return new ArrayList<>(youthDefendantIds);
        }

        public void setYouthDefendantIds(final List<String> youthDefendantIds) {
            this.youthDefendantIds = new ArrayList<>(youthDefendantIds);
        }

        public YouthCourt getYouthCourt() {
            return youthCourt;
        }

        private Boolean isBoxHearing;

        public TimelineHearingSummaryBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public TimelineHearingSummaryBuilder withHearingDate(final LocalDate hearingDate) {
            this.hearingDate = hearingDate;
            return this;
        }

        public TimelineHearingSummaryBuilder withHearingType(final String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public TimelineHearingSummaryBuilder withCourtHouse(final String courtHouse) {
            this.courtHouse = courtHouse;
            return this;
        }

        public TimelineHearingSummaryBuilder withCourtRoom(final String courtRoom) {
            this.courtRoom = courtRoom;
            return this;
        }

        public TimelineHearingSummaryBuilder withHearingTime(final ZonedDateTime hearingTime) {
            this.hearingTime = hearingTime;
            return this;
        }

        public TimelineHearingSummaryBuilder withStartTime(final ZonedDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public TimelineHearingSummaryBuilder withEstimatedDuration(final Integer estimatedDuration) {
            this.estimatedDuration = estimatedDuration;
            return this;
        }

        public TimelineHearingSummaryBuilder withDefendants(final List<String> defendants) {
            this.defendants = defendants;
            return this;
        }
        public TimelineHearingSummaryBuilder withApplicants(final List<String> applicants) {
            this.applicants = applicants;
            return this;
        }

        public TimelineHearingSummaryBuilder withYouthCourt(final YouthCourt youthCourt) {
            this.youthCourt = youthCourt;
            return this;
        }

        public TimelineHearingSummaryBuilder withYouthDefendantIds(final List<String> youthDefendantIds) {
            this.youthDefendantIds = new ArrayList<>(youthDefendantIds);
            return this;
        }
        public TimelineHearingSummaryBuilder withOutcome(final String outcome) {
            this.outcome = outcome;
            return this;
        }

        public TimelineHearingSummaryBuilder withIsBoxHearing(final Boolean isBoxHearing){
            this.isBoxHearing = isBoxHearing;
            return this;
        }

        public TimelineHearingSummary build() {
            return new TimelineHearingSummary(this);
        }
    }
}