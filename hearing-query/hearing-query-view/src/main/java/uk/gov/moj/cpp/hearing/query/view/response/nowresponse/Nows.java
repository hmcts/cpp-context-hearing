
package uk.gov.moj.cpp.hearing.query.view.response.nowresponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class Nows {

    @JsonProperty("id")
    private String id;

    @JsonProperty("defendantId")
    private String defendantId;

    @JsonProperty("nowsTypeId")
    private String nowsTypeId;

    @JsonProperty("material")
    private List<Material> material = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(String defendantId) {
        this.defendantId = defendantId;
    }

    public String getNowsTypeId() {
        return nowsTypeId;
    }

    public void setNowsTypeId(String nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
    }

    public List<Material> getMaterial() {
        return material;
    }

    public void setMaterial(List<Material> material) {
        this.material = material;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        private String id;
        private String defendantId;
        private String nowsTypeId;
        private List<Material> material = new ArrayList<>();

        private Builder() {
        }



        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withDefendantId(String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withNowsTypeId(String nowsTypeId) {
            this.nowsTypeId = nowsTypeId;
            return this;
        }

        public Builder withMaterial(List<Material> material) {
            this.material = material;
            return this;
        }

        public Nows build() {
            Nows nows = new Nows();
            nows.setId(id);
            nows.setDefendantId(defendantId);
            nows.setNowsTypeId(nowsTypeId);
            nows.setMaterial(material);
            return nows;
        }
    }
}
