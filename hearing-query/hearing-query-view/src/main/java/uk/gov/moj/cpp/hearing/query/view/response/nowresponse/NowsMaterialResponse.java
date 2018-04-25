
package uk.gov.moj.cpp.hearing.query.view.response.nowresponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


public class NowsMaterialResponse {

    @JsonProperty("material")
    private List<Material> material = new ArrayList<>();


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
        private List<Material> material = new ArrayList<>();

        private Builder() {
        }

        public Builder withMaterial(List<Material> material) {
            this.material = material;
            return this;
        }

        public NowsMaterialResponse build() {
            NowsMaterialResponse nowsMaterialResponse = new NowsMaterialResponse();
            nowsMaterialResponse.setMaterial(material);
            return nowsMaterialResponse;
        }
    }
}
