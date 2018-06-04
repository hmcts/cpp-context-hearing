package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class GenerateNowsCommand {

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
