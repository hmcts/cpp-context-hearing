package uk.gov.moj.cpp.hearing.command.result;

import java.time.LocalDate;
import java.util.UUID;

public final class DeleteDraftResultV2Command {

    private UUID hearingId;
    private LocalDate hearingDay;


    public static DeleteDraftResultV2Command deleteDraftResultCommand() {
        return new DeleteDraftResultV2Command();
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public DeleteDraftResultV2Command setHearingDay(LocalDate hearingDay) {
        this.hearingDay = hearingDay;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public DeleteDraftResultV2Command setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }


}