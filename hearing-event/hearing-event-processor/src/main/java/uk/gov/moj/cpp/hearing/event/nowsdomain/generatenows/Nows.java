package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.util.List;
import java.util.UUID;

public class Nows {

    private UUID id;

    private String nowsTypeId;

    private String defendantId;

    private List<Material> material;

    public UUID getId() {
        return this.id;
    }

    public Nows setId(UUID id) {
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

    public List<Material> getMaterial() {
        return this.material;
    }

    public Nows setMaterial(List<Material> material) {
        this.material = material;
        return this;
    }

    public static Nows nows() {
        return new Nows();
    }
}
