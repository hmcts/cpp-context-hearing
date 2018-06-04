package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class DefendantRef {

    private java.util.UUID defendantId;

    public static DefendantRef defendantRef() {
        return new DefendantRef();
    }

    public java.util.UUID getDefendantId() {
        return this.defendantId;
    }

    public DefendantRef setDefendantId(java.util.UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }
}
