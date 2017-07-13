package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultPromptKey {

    RESULT_DEFINITION_LABEL(0),
    LABEL(1),
    MANDATORY(2),
    TYPE(3),
    DURATION_ELEMENT(4),
    KEYWORDS(5),
    FIXED_LIST_ID(6);
    private int order;

    ResultPromptKey(final int orderP) {
        this.order = orderP;
    }

    public int getOrder() {
        return order;
    }
}
