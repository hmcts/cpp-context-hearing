package uk.gov.moj.cpp.hearing.nows.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.Hearing;

import java.io.Serializable;
import java.util.List;

@Event("hearing.events.nows-requested")
public class NowsRequested implements Serializable {
    private final static long serialVersionUID = 2L;

    private uk.gov.justice.json.schemas.core.Hearing hearing;

    private List<Now> nows;

    private List<NowType> nowTypes;

    private List<SharedResultLine> sharedResultLines;

    private CourtClerk courtClerk;

    public NowsRequested() {

    }

    @JsonCreator
    public NowsRequested(@JsonProperty("hearing") final Hearing hearing,
                         @JsonProperty("nows") List<Now> nows,
                         @JsonProperty("nowTypes") List<NowType> nowTypes,
                         @JsonProperty("sharedResultLines") List<SharedResultLine> sharedResultLines) {
        this.hearing = hearing;
        this.nows = nows;
        this.nowTypes = nowTypes;
        this.sharedResultLines = sharedResultLines;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public List<Now> getNows() {
        return nows;
    }

    public List<NowType> getNowTypes() {
        return nowTypes;
    }

    public List<SharedResultLine> getSharedResultLines() {
        return sharedResultLines;
    }

    public NowsRequested setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public NowsRequested setNows(List<Now> nows) {
        this.nows = nows;
        return this;
    }

    public NowsRequested setNowTypes(List<NowType> nowTypes) {
        this.nowTypes = nowTypes;
        return this;
    }

    public NowsRequested setSharedResultLines(List<SharedResultLine> sharedResultLines) {
        this.sharedResultLines = sharedResultLines;
        return this;
    }

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public NowsRequested setCourtClerk(CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public static NowsRequested nowsRequested() {
        return new NowsRequested();
    }
}
