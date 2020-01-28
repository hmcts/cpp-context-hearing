package uk.gov.moj.cpp.hearing.repository;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public class HearingEventPojo implements Serializable {
  private static final long serialVersionUID = 8135826356497097890L;

  private UUID defenceCounselId;

  private boolean deleted;

  private LocalDate eventDate;

  private ZonedDateTime eventTime;

  private UUID hearingEventDefinitionId;

  private UUID hearingId;

  private UUID id;

  private ZonedDateTime lastModifiedTime;

  private String recordedLabel;

  public HearingEventPojo(final UUID defenceCounselId,
                          final boolean deleted,
                          final LocalDate eventDate,
                          final ZonedDateTime eventTime,
                          final UUID hearingEventDefinitionId,
                          final UUID hearingId,
                          final UUID id,
                          final ZonedDateTime lastModifiedTime,
                          final String recordedLabel) {
    this.defenceCounselId = defenceCounselId;
    this.deleted = deleted;
    this.eventDate = eventDate;
    this.eventTime = eventTime;
    this.hearingEventDefinitionId = hearingEventDefinitionId;
    this.hearingId = hearingId;
    this.id = id;
    this.lastModifiedTime = lastModifiedTime;
    this.recordedLabel = recordedLabel;
  }

  public UUID getDefenceCounselId() {
    return defenceCounselId;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public LocalDate getEventDate() {
    return eventDate;
  }

  public ZonedDateTime getEventTime() {
    return eventTime;
  }

  public UUID getHearingEventDefinitionId() {
    return hearingEventDefinitionId;
  }

  public UUID getHearingId() {
    return hearingId;
  }

  public UUID getId() {
    return id;
  }

  public ZonedDateTime getLastModifiedTime() {
    return lastModifiedTime;
  }

  public String getRecordedLabel() {
    return recordedLabel;
  }

  @Override
  @SuppressWarnings({"squid:S00121","squid:S00122","squid:S1067"})
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    final HearingEventPojo that = (HearingEventPojo) obj;

    return
    java.util.Objects.equals(this.defenceCounselId, that.defenceCounselId) &&
    java.util.Objects.equals(this.deleted, that.deleted) &&
    java.util.Objects.equals(this.eventDate, that.eventDate) &&
    java.util.Objects.equals(this.eventTime, that.eventTime) &&
    java.util.Objects.equals(this.hearingEventDefinitionId, that.hearingEventDefinitionId) &&
    java.util.Objects.equals(this.hearingId, that.hearingId) &&
    java.util.Objects.equals(this.id, that.id) &&
    java.util.Objects.equals(this.lastModifiedTime, that.lastModifiedTime) &&
    java.util.Objects.equals(this.recordedLabel, that.recordedLabel);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(defenceCounselId, deleted, eventDate, eventTime, hearingEventDefinitionId, hearingId, id, lastModifiedTime, recordedLabel);
  }

  @Override
  public String toString() {
    return "HearingEvent{" +
    	"defenceCounselId='" + defenceCounselId + "'," +
    	"deleted='" + deleted + "'," +
    	"eventDate='" + eventDate + "'," +
    	"eventTime='" + eventTime + "'," +
    	"hearingEventDefinitionId='" + hearingEventDefinitionId + "'," +
    	"hearingId='" + hearingId + "'," +
    	"id='" + id + "'," +
    	"lastModifiedTime='" + lastModifiedTime + "'," +
    	"recordedLabel='" + recordedLabel + "'" +
    "}";
  }

  public HearingEventPojo setDefenceCounselId(UUID defenceCounselId) {
    this.defenceCounselId = defenceCounselId;
    return this;
  }

  public HearingEventPojo setDeleted(boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  public HearingEventPojo setEventDate(LocalDate eventDate) {
    this.eventDate = eventDate;
    return this;
  }

  public HearingEventPojo setEventTime(ZonedDateTime eventTime) {
    this.eventTime = eventTime;
    return this;
  }

  public HearingEventPojo setHearingEventDefinitionId(UUID hearingEventDefinitionId) {
    this.hearingEventDefinitionId = hearingEventDefinitionId;
    return this;
  }

  public HearingEventPojo setHearingId(UUID hearingId) {
    this.hearingId = hearingId;
    return this;
  }

  public HearingEventPojo setId(UUID id) {
    this.id = id;
    return this;
  }

  public HearingEventPojo setLastModifiedTime(ZonedDateTime lastModifiedTime) {
    this.lastModifiedTime = lastModifiedTime;
    return this;
  }

  public HearingEventPojo setRecordedLabel(String recordedLabel) {
    this.recordedLabel = recordedLabel;
    return this;
  }
}
