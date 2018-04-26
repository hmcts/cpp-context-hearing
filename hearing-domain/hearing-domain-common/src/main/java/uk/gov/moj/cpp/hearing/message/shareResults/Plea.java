package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class Plea {

    private UUID id;
    private LocalDate date;
    private String value;
    private UUID enteredHearingId;

    public UUID getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getValue() {
        return value;
    }

    public UUID getEnteredHearingId() {
        return enteredHearingId;
    }

    public Plea setId(UUID id) {
        this.id = id;
        return this;
    }

    public Plea setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public Plea setValue(String value) {
        this.value = value;
        return this;
    }

    public Plea setEnteredHearingId(UUID enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
        return this;
    }

    public static Plea plea() {
        return new Plea();
    }
}