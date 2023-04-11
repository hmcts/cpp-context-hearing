package uk.gov.moj.cpp.hearing.common.util;

public enum ObjectTypes {
    CASE("Case");

    private final String objectName;

    ObjectTypes(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String toString() {
        return objectName;
    }

    public String getObjectName() {
        return objectName;
    }
}

