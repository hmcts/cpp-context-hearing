package uk.gov.moj.cpp.hearing.command.result;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.JsonObject;

public final class SaveDraftResultV2Command {

    private UUID hearingId;
    private LocalDate hearingDay;
    private List<Relation> relations = new ArrayList<>();
    private Map<UUID, JsonObject> resultLines = new HashMap<>();
    private List<UUID> shadowListedOffenceIds = new ArrayList<>();

    public static SaveDraftResultV2Command saveDraftResultCommand() {
        return new SaveDraftResultV2Command();
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

    public SaveDraftResultV2Command setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public List<Relation> getRelations() {
        return new ArrayList<>(relations);
    }

    public void setRelations(List<Relation> relations) {
        this.relations = new ArrayList<>(relations);
    }

    public Map<UUID, JsonObject> getResultLines() {
        return resultLines;
    }

    public void setResultLines(Map<UUID, JsonObject> resultLines) {
        this.resultLines = resultLines;
    }

    public List<UUID> getShadowListedOffenceIds() {
        return new ArrayList<>(shadowListedOffenceIds);
    }

    public void setShadowListedOffenceIds(List<UUID> shadowListedOffenceIds) {
        this.shadowListedOffenceIds = new ArrayList<>(shadowListedOffenceIds);
    }
}