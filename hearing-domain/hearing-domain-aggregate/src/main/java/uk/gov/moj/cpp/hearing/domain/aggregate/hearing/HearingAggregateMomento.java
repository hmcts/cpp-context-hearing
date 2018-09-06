package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HearingAggregateMomento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<UUID, HearingEventDelegate.HearingEvent> hearingEvents = new HashMap<>();
    private final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels = new HashMap<>();
    private final Map<UUID, DefenceCounselUpsert> defenceCounsels = new HashMap<>();
    private final Map<UUID, Plea> pleas = new HashMap<>();
    private final Map<UUID, VerdictUpsert> verdicts = new HashMap<>();
    private Hearing hearing;
    private List<Variant> variantDirectory = new ArrayList<>();
    private final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus = new HashMap<>();
    private Map<UUID, CompletedResultLine> completedResultLines = new HashMap<>();
    private List<UUID> adjournedHearingIds = new ArrayList<>();
    private Map<UUID, Target> targets = new HashMap<>();

    private boolean published = false;

    public Map<UUID, HearingEventDelegate.HearingEvent> getHearingEvents() {
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

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public void setVariantDirectory(Collection<Variant> variantDirectory) {
        this.variantDirectory = new ArrayList<>(variantDirectory);
    }

    public List<Variant> getVariantDirectory() {
        return variantDirectory;
    }

    public Map<UUID, CompletedResultLineStatus> getCompletedResultLinesStatus() {
        return completedResultLinesStatus;
    }

    public Map<UUID, CompletedResultLine> getCompletedResultLines() {
        return completedResultLines;
    }

    public void setCompletedResultLines(Map<UUID, CompletedResultLine> completedResultLines) {
        this.completedResultLines = completedResultLines;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public List<UUID> getAdjournedHearingIds() {
        return adjournedHearingIds;
    }

    public void setAdjournedHearingIds(final List<UUID> adjournedHearingIds) {
        this.adjournedHearingIds = adjournedHearingIds;
    }

    public Map<UUID, Target> getTargets() {
        return targets;
    }

    public void setTargets(Map<UUID, Target> targets) {
        this.targets = targets;
    }
}
