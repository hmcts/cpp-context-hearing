package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultType {
    TXT, CURR, INT, BOOLEAN, DURATION, TIME, DATE, RESULT, IGNORED, FIXL, FIXLM,FIXLO,FIXLOM;

    public static boolean isFixedListType(ResultType type) {
        return type == FIXL || type == FIXLM || type == FIXLO || type == FIXLOM;
    }
}
