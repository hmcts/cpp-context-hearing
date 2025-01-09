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
    private Integer version;
    private List<Relation> relations = new ArrayList<>();
    private Map<UUID, JsonObject> resultLines = new HashMap<>();
    private List<UUID> shadowListedOffenceIds = new ArrayList<>();

    public static SaveDraftResultV2Command saveDraftResultCommand() {
        return new SaveDraftResultV2Command();
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public SaveDraftResultV2Command setHearingDay(LocalDate hearingDay) {
        this.hearingDay = hearingDay;
        return this;
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

    public SaveDraftResultV2Command setRelations(List<Relation> relations) {
        this.relations = new ArrayList<>(relations);
        return this;
    }

    public Map<UUID, JsonObject> getResultLines() {
        return resultLines;
    }

    public SaveDraftResultV2Command setResultLines(Map<UUID, JsonObject> resultLines) {
        this.resultLines = resultLines;
        return this;
    }

    public List<UUID> getShadowListedOffenceIds() {
        return new ArrayList<>(shadowListedOffenceIds);
    }

    public void setShadowListedOffenceIds(List<UUID> shadowListedOffenceIds) {
        this.shadowListedOffenceIds = new ArrayList<>(shadowListedOffenceIds);
    }

    public Integer getVersion() {
        return version;
    }

    public SaveDraftResultV2Command setVersion(final Integer version) {
        this.version = version;
        return this;
    }
}