package uk.gov.moj.cpp.hearing.domain.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.ResultLine;

@Event("hearing.results-shared")
public class ResultsShared implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final ZonedDateTime sharedTime;
    private final List<ResultLine> resultLines;
    private final Hearing hearing;
    private final List<Case> cases;
    private final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels;
    private final Map<UUID, DefenceCounselUpsert> defenceCounsels;
    private final Map<UUID, VerdictUpsert> verdicts;
    private final Map<UUID, Plea> pleas;

    @JsonCreator
    public ResultsShared(@JsonProperty("hearingId") final UUID hearingId,
                         @JsonProperty("sharedTime") final ZonedDateTime sharedTime,
                         @JsonProperty("resultLines") final List<ResultLine> resultLines,
                         @JsonProperty("hearing") final Hearing hearing,
                         @JsonProperty("cases") final List<Case> cases,
                         @JsonProperty("prosecutionCounsels") final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels,
                         @JsonProperty("defenceCounsels") final Map<UUID, DefenceCounselUpsert> defenceCounsels,
                         @JsonProperty("pleas") final Map<UUID, Plea> pleas,
                         @JsonProperty("verdicts") final Map<UUID, VerdictUpsert> verdicts
                         ) {
        this.hearingId = hearingId;
        this.sharedTime = sharedTime;
        this.resultLines = Collections.unmodifiableList(Optional.ofNullable(resultLines).orElseGet(ArrayList::new));
        this.cases = Collections.unmodifiableList(Optional.ofNullable(cases).orElseGet(ArrayList::new));
        this.hearing = hearing;
        this.prosecutionCounsels = prosecutionCounsels;
        this.defenceCounsels = defenceCounsels;
        this.pleas = pleas;
        this.verdicts = verdicts;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public List<Case> getCases() {
        return cases;
    }

    public Map<UUID, ProsecutionCounselUpsert> getProsecutionCounsels() {
        return prosecutionCounsels;
    }

    public Map<UUID, DefenceCounselUpsert> getDefenceCounsels() {
        return defenceCounsels;
    }

    public Map<UUID, VerdictUpsert> getVerdicts() {
        return verdicts;
    }

    public Map<UUID, Plea> getPleas() {
        return pleas;
    }
}