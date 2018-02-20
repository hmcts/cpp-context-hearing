package uk.gov.moj.cpp.hearing.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class RecordMagsCourtHearingCommand {
    private Hearing hearing;
    public RecordMagsCourtHearingCommand(final Hearing hearing) {
        this.hearing=hearing;
    }
    public Hearing getHearing() {
        return hearing;
    }

}
