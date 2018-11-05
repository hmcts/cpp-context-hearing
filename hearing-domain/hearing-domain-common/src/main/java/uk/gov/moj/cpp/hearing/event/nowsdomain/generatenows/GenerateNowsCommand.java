package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;


public class GenerateNowsCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private Hearing hearing;

    public static GenerateNowsCommand generateNowsCommand() {
        return new GenerateNowsCommand();
    }

    public Hearing getHearing() {
        return this.hearing;
    }

    public GenerateNowsCommand setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }
}
