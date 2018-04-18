package uk.gov.justice.ccr.notepad.result.cache.model;


public enum ResultPromptKey {

    NO_NAME(0),
    RESULT_DEFINITION_UUID(1),
    RESULT_DEFINITION_LABEL(2),
    UUID(3),
    LABEL(4),
    MANDATORY(5),
    PROMPT_TYPE(6),
    DURATION_ELEMENT(7),
    FIXED_LIST_UUID(8),
    FIXED_LIST_LABEL(9),
    MIN(10),
    MAX(11),
    QUAL(12),
    RESULT_PROMPT_GROUP(13),
    WORDING(14),
    WELSH_WORDING(15),
    PROMPT_SEQUENCE(16),
    RESULT_PROMPT_WORD_GROUP(17),
    JURISDICTION(18),
    PROMPT_REFERENCE(19),
    USER_GROUPS(20);

    private int order;

    ResultPromptKey(final int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
