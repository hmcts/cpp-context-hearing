package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Now implements Serializable {

    private final static long serialVersionUID = 2L;
    private UUID id;
    private UUID nowsTypeId;
    private String nowsTemplateName;
    private UUID defendantId;
    private List<Material> materials = new ArrayList<Material>();

    public UUID getNowsTypeId() {
        return nowsTypeId;
    }

    public Now setNowsTypeId(UUID nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public Now setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getNowsTemplateName() {
        return nowsTemplateName;
    }

    public Now setNowsTemplateName(String nowsTemplateName) {
        this.nowsTemplateName = nowsTemplateName;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Now setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public Now setMaterials(List<Material> materials) {
        this.materials = materials;
        return this;
    }

    public static Now now(){
        return new Now();
    }
}
