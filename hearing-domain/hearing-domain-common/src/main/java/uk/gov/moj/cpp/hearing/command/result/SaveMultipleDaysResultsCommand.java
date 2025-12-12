package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.Target;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class SaveMultipleDaysResultsCommand implements Serializable {

    private UUID hearingId;
    private List<Target> targets;
    private LocalDate hearingDay;

    public SaveMultipleDaysResultsCommand(final UUID hearingId, final List<Target> targets, final LocalDate hearingDay) {
        this.hearingId = hearingId;
        this.targets = targets;
        this.hearingDay = hearingDay;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }


    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public void setHearingDay(final LocalDate hearingDay) {
        this.hearingDay = hearingDay;
    }
}
