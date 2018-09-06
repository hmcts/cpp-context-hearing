package uk.gov.moj.cpp.hearing.domain.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.CourtCentre;
import uk.gov.moj.cpp.hearing.domain.HearingType;

@SuppressWarnings({"squid:S00107"})
@Event("hearing.hearing-event-logged")
public class HearingEventLogged implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingEventId;
    private final UUID lastHearingEventId;
    private final UUID hearingId;
    private final UUID hearingEventDefinitionId;
    private final UUID defenceCounselId;
    private final String recordedLabel;
    private final ZonedDateTime eventTime;
    private final ZonedDateTime lastModifiedTime;
    private final boolean alterable;
    private final CourtCentre courtCentre;
    private final HearingType hearingType;
    private final String caseURN;

    @JsonCreator
    public HearingEventLogged(
            @JsonProperty("hearingEventId") final UUID hearingEventId,
            @JsonProperty("lastHearingEventId") final UUID lastHearingEventId,
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("hearingEventDefinitionId") final UUID hearingEventDefinitionId,
            @JsonProperty("defenceCounselId") final UUID defenceCounselId,
            @JsonProperty("recordedLabel") final String recordedLabel,
            @JsonProperty("eventTime") final ZonedDateTime eventTime,
            @JsonProperty("lastModifiedTime") final ZonedDateTime lastModifiedTime,
            @JsonProperty("alterable") final boolean alterable,
            @JsonProperty("courtCentre") final CourtCentre courtCentre,
            @JsonProperty("hearingType") final HearingType hearingType,
            @JsonProperty("caseURN") final String caseURN) {
        this.hearingEventId = hearingEventId;
        this.lastHearingEventId = lastHearingEventId;
        this.hearingId = hearingId;
        this.hearingEventDefinitionId = hearingEventDefinitionId;
        this.recordedLabel = recordedLabel;
        this.eventTime = eventTime;
        this.lastModifiedTime = lastModifiedTime;
        this.alterable = alterable;
        this.courtCentre = courtCentre;
        this.hearingType = hearingType;
        this.caseURN = caseURN;
        this.defenceCounselId = defenceCounselId;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public UUID getLastHearingEventId() {
        return lastHearingEventId;
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

    public UUID getHearingEventDefinitionId() {
        return hearingEventDefinitionId;
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public HearingType getHearingType() {
        return hearingType;
    }

    public String getCaseURN() {
        return caseURN;
    }

    public UUID getDefenceCounselId() {
        return defenceCounselId;
    }

}
