package uk.gov.moj.cpp.hearing.command;

import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class RecordMagsCourtHearingCommand {
    private Hearing hearing;

    @JsonCreator
    public RecordMagsCourtHearingCommand(@JsonProperty("hearing") final Hearing hearing) {
        this.hearing = hearing;
    }

    public Hearing getHearing() {
        return hearing;
    }

}
