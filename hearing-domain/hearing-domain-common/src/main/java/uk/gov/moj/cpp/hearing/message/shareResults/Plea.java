package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;

public class Plea {

    private LocalDate pleaDate;
    private String pleaValue;

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getPleaValue() {
        return pleaValue;
    }

    public Plea setPleaDate(LocalDate pleaDate) {
        this.pleaDate = pleaDate;
        return this;
    }

    public Plea setPleaValue(String pleaValue) {
        this.pleaValue = pleaValue;
        return this;
    }

    public static Plea plea(){
        return new Plea();
    }


}
