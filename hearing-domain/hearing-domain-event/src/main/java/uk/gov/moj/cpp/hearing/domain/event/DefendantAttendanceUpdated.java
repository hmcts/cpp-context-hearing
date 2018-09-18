package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.AttendanceDay;
import java.io.Serializable;
import java.util.UUID;

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

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public AttendanceDay getAttendanceDay() {
        return attendanceDay;
    }

    public DefendantAttendanceUpdated setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public DefendantAttendanceUpdated setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public DefendantAttendanceUpdated setAttendanceDay(AttendanceDay attendanceDay) {
        this.attendanceDay = attendanceDay;
        return this;
    }

    public static DefendantAttendanceUpdated defendantAttendanceUpdated() {
        return new DefendantAttendanceUpdated();
    }

}
