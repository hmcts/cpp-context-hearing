package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Nows {

    private java.util.UUID id;

    private String nowsTypeId;

    private String defendantId;

    private java.util.List<Material> material;

    public static Nows nows() {
        return new Nows();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Nows setId(java.util.UUID id) {
        this.id = id;
        return this;
    }

    public String getNowsTypeId() {
        return this.nowsTypeId;
    }

    public Nows setNowsTypeId(String nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
        return this;
    }

    public String getDefendantId() {
        return this.defendantId;
    }

    public Nows setDefendantId(String defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public java.util.List<Material> getMaterial() {
        return this.material;
    }

    public Nows setMaterial(java.util.List<Material> material) {
        this.material = material;
        return this;
    }
}
