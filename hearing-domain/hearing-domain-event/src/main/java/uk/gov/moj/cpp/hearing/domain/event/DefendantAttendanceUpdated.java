package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.AttendanceDay;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.defendant-attendance-updated")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAttendanceUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID defendantId;
    private AttendanceDay attendanceDay;

    public DefendantAttendanceUpdated() {
    }

    @JsonCreator
    protected DefendantAttendanceUpdated(@JsonProperty("hearingId") final UUID hearingId,
                                         @JsonProperty("defendantId") final UUID defendantId,
                                         @JsonProperty("attendanceDay") final AttendanceDay attendanceDay) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.attendanceDay = attendanceDay;
    }

    public static DefendantAttendanceUpdated defendantAttendanceUpdated() {
        return new DefendantAttendanceUpdated();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public DefendantAttendanceUpdated setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public DefendantAttendanceUpdated setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public AttendanceDay getAttendanceDay() {
        return attendanceDay;
    }

    public DefendantAttendanceUpdated setAttendanceDay(AttendanceDay attendanceDay) {
        this.attendanceDay = attendanceDay;
        return this;
    }

}
