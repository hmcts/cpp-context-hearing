package uk.gov.moj.cpp;


import java.io.Serializable;
import java.util.Optional;

public enum OperationTypeEnum implements Serializable {

    TEST("test"),

    REMOVE("remove"),

    ADD("add"),

    REPLACE("replace"),

    MOVE("move"),

    COPY("copy"),
    ;

    private final String value;

    OperationTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Optional<OperationTypeEnum> valueFor(final String value) {
        if (TEST.value.equals(value)) {
            return Optional.of(TEST);
        }
        if (REMOVE.value.equals(value)) {
            return Optional.of(REMOVE);
        }
        if (ADD.value.equals(value)) {
            return Optional.of(ADD);
        }
        if (REPLACE.value.equals(value)) {
            return Optional.of(REPLACE);
        }
        if (MOVE.value.equals(value)) {
            return Optional.of(MOVE);
        }
        if (COPY.value.equals(value)) {
            return Optional.of(COPY);
        }

        return Optional.empty();
    }
}
