package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ha_hearing_event")
public class HearingEvent {

    @Id
    private UUID id;

    @Column(name = "hearing_event_definition_id")
    private UUID hearingEventDefinitionId;

    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "recorded_label")
    private String recordedLabel;

    @Column(name = "event_time")
    private ZonedDateTime eventTime;

    @Column(name = "last_modified_time")
    private ZonedDateTime lastModifiedTime;

    @Column(name = "alterable")
    private boolean alterable;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "defence_counsel_id")
    private UUID defenceCounselId;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "note")
    private String note;

    @Column(name = "user_id")
    private UUID userId;

    public HearingEvent() {
        // for JPA
    }

    public static HearingEvent hearingEvent() {
        return new HearingEvent();
    }

    public UUID getId() {
        return id;
    }

    public HearingEvent setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingEvent setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public HearingEvent setRecordedLabel(final String recordedLabel) {
        this.recordedLabel = recordedLabel;
        return this;
    }

    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public HearingEvent setEventTime(final ZonedDateTime eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    public ZonedDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public HearingEvent setLastModifiedTime(final ZonedDateTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public HearingEvent setAlterable(final boolean alterable) {
        this.alterable = alterable;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public HearingEvent setDeleted(final boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }

    public HearingEvent setHearingEventDefinitionId(final UUID hearingEventDefinitionId) {
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        return this;
    }

    public UUID getDefenceCounselId() {
        return defenceCounselId;
    }

    public HearingEvent setDefenceCounselId(final UUID counselId) {
        this.defenceCounselId = counselId;
        return this;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public HearingEvent setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    public String getNote() {
        return note;
    }

    public HearingEvent setNote(String note) {
        this.note = note;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public HearingEvent setUserId(final UUID userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HearingEvent that = (HearingEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
