package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class Plea {

    private UUID id;
    private LocalDate date;
    private String value;
    private UUID enteredHearingId;

    public static Plea plea() {
        return new Plea();
    }

    public UUID getId() {
        return id;
    }

    public Plea setId(UUID id) {
        this.id = id;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public Plea setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Plea setValue(String value) {
        this.value = value;
        return this;
    }

    public UUID getEnteredHearingId() {
        return enteredHearingId;
    }

    public Plea setEnteredHearingId(UUID enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
        return this;
    }
}