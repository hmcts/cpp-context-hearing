package uk.gov.moj.cpp.hearing.command.defendant;

import uk.gov.justice.core.courts.AttendanceDay;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateDefendantAttendanceCommand {

    private UUID hearingId;
    private UUID defendantId;
    private AttendanceDay attendanceDay;

    public UpdateDefendantAttendanceCommand() {
    }

    @JsonCreator
    public UpdateDefendantAttendanceCommand(@JsonProperty("hearingId") UUID hearingId,
                                            @JsonProperty("defendantId") UUID defendantId,
                                            @JsonProperty("attendanceDay") AttendanceDay attendanceDay) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.attendanceDay = attendanceDay;
    }

    public static UpdateDefendantAttendanceCommand updateDefendantAttendanceCommand() {
        return new UpdateDefendantAttendanceCommand();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UpdateDefendantAttendanceCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UpdateDefendantAttendanceCommand setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public AttendanceDay getAttendanceDay() {
        return attendanceDay;
    }

    public UpdateDefendantAttendanceCommand setAttendanceDay(AttendanceDay attendanceDay) {
        this.attendanceDay = attendanceDay;
        return this;
    }
}
