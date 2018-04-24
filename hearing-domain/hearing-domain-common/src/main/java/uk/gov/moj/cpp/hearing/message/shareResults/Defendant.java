package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.List;

public class Defendant {

    private List<ResultLine> resultsLines;

    private Person person;

    private Hearing hearing;

    public List<ResultLine> getResultsLines() {
        return resultsLines;
    }

    public Person getPerson() {
        return person;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public Defendant setResultsLines(List<ResultLine> resultsLines) {
        this.resultsLines = resultsLines;
        return this;
    }

    public Defendant setPerson(Person person) {
        this.person = person;
        return this;
    }

    public Defendant setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public static Defendant defendant(){
        return new Defendant();
    }

}
