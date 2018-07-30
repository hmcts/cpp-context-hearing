package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class Plea implements Serializable {
    private final static long serialVersionUID = 6227964658664505863L;

    private String id;
    private String value;
    private String date;
    private String enteredHearingId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEnteredHearingId() {
        return enteredHearingId;
    }

    public void setEnteredHearingId(String enteredHearingId) {
        this.enteredHearingId = enteredHearingId;
    }
}
