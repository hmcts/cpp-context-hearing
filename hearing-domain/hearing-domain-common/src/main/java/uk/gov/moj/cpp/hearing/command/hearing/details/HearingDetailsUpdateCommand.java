package uk.gov.moj.cpp.hearing.command.hearing.details;

import com.fasterxml.jackson.annotation.JsonCreator;

public class HearingDetailsUpdateCommand {

    private Hearing hearing;

    public HearingDetailsUpdateCommand() {
    }

    @JsonCreator
    public HearingDetailsUpdateCommand(Hearing hearing) {
        this.hearing = hearing;
    }

    public static HearingDetailsUpdateCommand hearingDetailsUpdateCommand() {
        return new HearingDetailsUpdateCommand();
    }

    public Hearing getHearing() {
        return hearing;
    }

    public HearingDetailsUpdateCommand setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }
}

