package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Nows implements Serializable {

    private UUID id;

    private UUID nowsTypeId;

    private String nowsTemplateName;

    private UUID defendantId;

    private List<Material> material;

    public static Nows nows() {
        return new Nows();
    }

    public UUID getId() {
        return this.id;
    }

    public Nows setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getNowsTypeId() {
        return this.nowsTypeId;
    }

    public Nows setNowsTypeId(UUID nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
        return this;
    }

    public String getNowsTemplateName() {
        return this.nowsTemplateName;
    }

    public Nows setNowsTemplateName(String nowsTemplateName) {
        this.nowsTemplateName = nowsTemplateName;
        return this;
    }

    public UUID getDefendantId() {
        return this.defendantId;
    }

    public Nows setDefendantId(UUID defendantId) {
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
}
