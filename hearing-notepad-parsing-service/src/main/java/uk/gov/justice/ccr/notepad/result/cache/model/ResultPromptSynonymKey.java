package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultPromptSynonymKey {

    WORD(0),
    SYNONYM(1);

    private int order;

    ResultPromptSynonymKey(final int orderP) {
        this.order = orderP;
    }

    public int getOrder() {
        return order;
    }
}
