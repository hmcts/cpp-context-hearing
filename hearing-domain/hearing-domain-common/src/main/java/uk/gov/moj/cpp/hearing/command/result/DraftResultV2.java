package uk.gov.moj.cpp.hearing.command.result;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.JsonObject;

@SuppressWarnings({"squid:S2384"})
public class DraftResultV2 {

    private UUID hearingId;
    private LocalDate hearingDay;
    private List<Relation> relations;
    private Map<UUID, JsonObject> resultLines = new HashMap<>();
    private List<UUID> shadowListedOffenceIds;

    public DraftResultV2() {
    }

    public DraftResultV2(final UUID hearingId,
                         final LocalDate hearingDay,
                         final List<Relation> relations,
                         final Map<UUID, JsonObject> resultLines,
                         final List<UUID> shadowListedOffenceIds) {

        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.relations = relations;
        this.resultLines = resultLines;
        this.shadowListedOffenceIds = shadowListedOffenceIds;
    }

    public static DraftResultV2 saveDraftResultCommand() {
        return new DraftResultV2();
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public void setHearingDay(LocalDate hearingDay) {
        this.hearingDay = hearingDay;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public DraftResultV2 setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public Map<UUID, JsonObject> getResultLines() {
        return resultLines;
    }

    public void setResultLines(Map<UUID, JsonObject> resultLines) {
        this.resultLines = resultLines;
    }

    public List<UUID> getShadowListedOffenceIds() {
        return shadowListedOffenceIds;
    }

    public void setShadowListedOffenceIds(List<UUID> shadowListedOffenceIds) {
        this.shadowListedOffenceIds = shadowListedOffenceIds;
    }
}
