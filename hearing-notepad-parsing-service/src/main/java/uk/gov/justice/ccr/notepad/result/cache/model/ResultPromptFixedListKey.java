package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultPromptFixedListKey {


    FIXED_LIST_ID(0),
    CODE(1),
    VALUE(2);
    
    private int order;

    ResultPromptFixedListKey(final int orderP) {
        this.order = orderP;
    }

    public int getOrder() {
        return order;
    }
}
