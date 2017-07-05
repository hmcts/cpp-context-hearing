package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultDefinitionSynonymKey {

    WORD(0),
    SYNONYM(1);

    private int order;

    ResultDefinitionSynonymKey(final int orderP) {
        this.order = orderP;
    }

    public int getOrder() {
        return order;
    }
}
