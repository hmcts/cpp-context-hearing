package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.ZonedDateTime;
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

    public HearingEvent() {
        // for JPA
    }

    public UUID getId() {
        return id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public ZonedDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }

    public HearingEvent setId(final UUID id) {
        this.id = id;
        return this;
    }

    public HearingEvent setHearingEventDefinitionId(final UUID hearingEventDefinitionId) {
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        return this;
    }

    public HearingEvent setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public HearingEvent setRecordedLabel(final String recordedLabel) {
        this.recordedLabel = recordedLabel;
        return this;
    }

    public HearingEvent setEventTime(final ZonedDateTime eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    public HearingEvent setLastModifiedTime(final ZonedDateTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    public HearingEvent setAlterable(final boolean alterable) {
        this.alterable = alterable;
        return this;
    }

    public HearingEvent setDeleted(final boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public UUID getDefenceCounselId() {
        return defenceCounselId;
    }

    public HearingEvent setDefenceCounselId(final UUID counselId) {
        this.defenceCounselId = counselId;
        return this;
    }

    public static HearingEvent hearingEvent(){
        return new HearingEvent();
    }
}
