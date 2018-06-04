package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Plea {

    private java.util.UUID id;

    private String value;

    private String date;

    private java.util.UUID enteredHearingId;

    public static Plea plea() {
        return new Plea();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Plea setId(java.util.UUID id) {
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

    public String getDate() {
        return this.date;
    }

    public Plea setDate(String date) {
        this.date = date;
        return this;
    }

    public java.util.UUID getEnteredHearingId() {
        return this.enteredHearingId;
    }

    public Plea setEnteredHearingId(java.util.UUID enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
        return this;
    }
}
