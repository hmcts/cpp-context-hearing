package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;

import java.util.List;

@Event("hearing.initiated")
public class Initiated {
//TODO - GPE-3032 CLEANUP - rename to HearingInitiated

    private List<Case> cases;
    private Hearing hearing;

    public Initiated(){

    }

    public Initiated(List<Case> cases, Hearing hearing){
        this.cases = cases;
        this.hearing = hearing;
    }

    public List<Case> getCases() {
        return cases;
    }

    public Hearing getHearing() {
        return hearing;
    }
}
