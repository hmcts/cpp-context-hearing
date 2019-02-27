package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultDefinitionKey {

    UUID(0),
    LABEL(1),
    SHORT_CODE(2),
    LEVEL(3),
    ADJOURNMENT(4),
    CATEGORY(5),
    URGENT(6),
    FINANCIAL(7),
    CONVICTED(8),
    D20(9),
    RESULT_DEFINITION_GROUP(10),
    DVLA(11),
    CJS_CODE(12),
    QUALIFIER(13),
    POST_HEARING_CUSTODY_STATUS(14),
    JURISDICTION(15),
    RANK(16),
    RESULT_WORD_GROUP(17),
    NO_NAME(18),
    L_CODE(19),
    WELSH_WORDING(20),
    START_DATE(21),
    END_DATE(22),
    TERMINATION_DATE(23),
    USER_GROUPS(24);

    private int order;

    ResultDefinitionKey(final int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
