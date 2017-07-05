package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultDefinitionKey {

    LABEL(0),
    SHORT_CODE(1),
    LEVEL(2),
    ADJOURNMENT(3),
    CATEGORY(4),
    URGENT(5),
    FINANCIAL(6),
    CONVICTED(7),
    D20(8),
    RESULT_DEFINITION_GROUP(9),
    DVLA(10),
    CJS_CODE(11),
    QUALIFIER(12),
    POST_HEARING_CUSTODY_STATUS(13),
    JURISDICTION(14),
    RESULT_SEQUENCE(15),
    KEYWORDS(16);

    private int order;

    ResultDefinitionKey(final int orderP) {
        this.order = orderP;
    }

    public int getOrder() {
        return order;
    }
}
