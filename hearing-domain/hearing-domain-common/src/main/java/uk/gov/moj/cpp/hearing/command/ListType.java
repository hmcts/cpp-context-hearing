package uk.gov.moj.cpp.hearing.command;

public enum ListType {
    TYPE_1_FIXED("1F"),
    TYPE_2_FLEXIBLE("2F");

    private final String code;

    ListType(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
