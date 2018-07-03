package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Plea implements Serializable {

    private UUID id;

    private String value;

    private LocalDate date;

    private UUID enteredHearingId;

    public static Plea plea() {
        return new Plea();
    }

    public UUID getId() {
        return this.id;
    }

    public Plea setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public Plea setValue(String value) {
        this.value = value;
        return this;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public Plea setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public UUID getEnteredHearingId() {
        return this.enteredHearingId;
    }

    public Plea setEnteredHearingId(UUID enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
        return this;
    }
}
