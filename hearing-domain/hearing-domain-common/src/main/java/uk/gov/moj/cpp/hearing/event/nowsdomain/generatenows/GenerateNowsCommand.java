package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.Hearing;

import java.io.Serializable;
import java.util.List;


public class GenerateNowsCommand implements Serializable {

    private static final long serialVersionUID = 2L;

    private uk.gov.justice.json.schemas.core.Hearing hearing;

    private List<Nows> nows;

    private List<NowTypes> nowTypes;

    private List<SharedResultLines> sharedResultLines;

    private CourtClerk courtClerk;

    public static GenerateNowsCommand generateNowsCommand() {
        return new GenerateNowsCommand();
    }

    public Hearing getHearing() {
        return this.hearing;
    }

    public GenerateNowsCommand setHearing(uk.gov.justice.json.schemas.core.Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public List<Nows> getNows() {
        return nows;
    }

    public GenerateNowsCommand setNows(List<Nows> nows) {
        this.nows = nows;
        return this;
    }

    public List<NowTypes> getNowTypes() {
        return nowTypes;
    }

    public GenerateNowsCommand setNowTypes(List<NowTypes> nowTypes) {
        this.nowTypes = nowTypes;
        return this;
    }

    public GenerateNowsCommand setSharedResultLines(List<SharedResultLines> sharedResultLines) {
        this.sharedResultLines = sharedResultLines;
        return this;
    }

    public List<SharedResultLines> getSharedResultLines() {
        return sharedResultLines;
    }

    public GenerateNowsCommand setCourtClerk(CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }
}
