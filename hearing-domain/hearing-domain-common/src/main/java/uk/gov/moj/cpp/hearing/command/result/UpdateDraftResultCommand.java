package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateDraftResultCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private LocalDate hearingDay;
    private List<Operation> operations = new ArrayList<>();

    public UpdateDraftResultCommand() {
    }

    @JsonCreator
    private UpdateDraftResultCommand(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("hearingDay") final LocalDate hearingDay,
            @JsonProperty("operations") final List<Operation> operations
    ) {
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.operations = operations;
    }

    public static UpdateDraftResultCommand updateDraftResultCommand() {
        return new UpdateDraftResultCommand();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UpdateDraftResultCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public UpdateDraftResultCommand setHearingDay(LocalDate hearingDay) {
        this.hearingDay = hearingDay;
        return this;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public UpdateDraftResultCommand setOperations(List<Operation> operations) {
        this.operations = operations;
        return this;
    }

}

