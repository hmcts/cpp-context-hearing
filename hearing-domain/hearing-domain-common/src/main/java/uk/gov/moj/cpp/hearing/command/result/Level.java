package uk.gov.moj.cpp.hearing.command.result;

public enum Level {

    DEFENDANT,
    CASE,
    OFFENCE;

    @Override
    public String toString() {
        return name();
    }
}