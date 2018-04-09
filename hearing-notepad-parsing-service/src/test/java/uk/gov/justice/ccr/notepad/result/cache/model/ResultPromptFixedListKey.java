package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultPromptFixedListKey {

    UUID(0),
    FIXED_LIST_ID(1),
    CODE(2),
    VALUE(3),
    CJS_QUALIFIER(4),
    START_DATE(5),
    END_DATE(6),
    TERMINATION_DATE(7);

    private int order;

    ResultPromptFixedListKey(final int orderP) {
        this.order = orderP;
    }

    public int getOrder() {
        return order;
    }
}
