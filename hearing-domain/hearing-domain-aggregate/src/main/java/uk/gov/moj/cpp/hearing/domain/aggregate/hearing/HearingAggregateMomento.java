package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HearingAggregateMomento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<UUID, NewModelHearingAggregate.HearingEvent> hearingEvents = new HashMap<>();
    private final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels = new HashMap<>();
    private final Map<UUID, DefenceCounselUpsert> defenceCounsels = new HashMap<>();
    private final Map<UUID, Plea> pleas = new HashMap<>();
    private final Map<UUID, VerdictUpsert> verdicts = new HashMap<>();
    private List<Case> cases;
    private Hearing hearing;
    private final Map<NewModelHearingAggregate.VariantKeyHolder, Variant> variantDirectory = new HashMap<>();
    private final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus = new HashMap<>();
    private final Map<UUID, CompletedResultLine> completedResultLines = new HashMap<>();

    private boolean published = false;

    public Map<UUID, NewModelHearingAggregate.HearingEvent> getHearingEvents() {
        return hearingEvents;
    }

    public Map<UUID, ProsecutionCounselUpsert> getProsecutionCounsels() {
        return prosecutionCounsels;
    }

    public Map<UUID, DefenceCounselUpsert> getDefenceCounsels() {
        return defenceCounsels;
    }

    public Map<UUID, Plea> getPleas() {
        return pleas;
    }

    public Map<UUID, VerdictUpsert> getVerdicts() {
        return verdicts;
    }

    public List<Case> getCases() {
        return cases;
    }

    public void setCases(List<Case> cases) {
        this.cases = cases;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public Map<NewModelHearingAggregate.VariantKeyHolder, Variant> getVariantDirectory() {
        return variantDirectory;
    }

    public Map<UUID, CompletedResultLineStatus> getCompletedResultLinesStatus() {
        return completedResultLinesStatus;
    }

    public Map<UUID, CompletedResultLine> getCompletedResultLines() {
        return completedResultLines;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
