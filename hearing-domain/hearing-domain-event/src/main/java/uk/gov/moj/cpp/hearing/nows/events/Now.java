package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Now implements Serializable {

    private final static long serialVersionUID = 7806708017927251823L;
    private String id;
    private String nowsTypeId;
    private String nowsTemplateName;
    private String defendantId;
    private List<Material> material = new ArrayList<Material>();

    public String getNowsTypeId() {
        return nowsTypeId;
    }

    public void setNowsTypeId(String nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNowsTemplateName() {
        return nowsTemplateName;
    }

    public void setNowsTemplateName(String nowsTemplateName) {
        this.nowsTemplateName = nowsTemplateName;
    }

    public String getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(String defendantId) {
        this.defendantId = defendantId;
    }

    public List<Material> getMaterial() {
        return material;
    }

    public void setMaterial(List<Material> material) {
        this.material = material;
    }
}
