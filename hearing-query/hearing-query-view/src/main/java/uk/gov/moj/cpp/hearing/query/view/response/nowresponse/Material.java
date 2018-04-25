
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


public class Material {

    @JsonProperty("id")
    private String id;
    @JsonProperty("defendantId")
    private String defendantId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("userGroups")
    private List<String> userGroups = new ArrayList<>();

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String defendantId;
        private String status;
        private List<String> userGroups = new ArrayList<>();

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

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withUserGroups(List<String> userGroups) {
            this.userGroups = userGroups;
            return this;
        }

        public Material build() {
            Material material = new Material();
            material.setId(id);
            material.setDefendantId(defendantId);
            material.setStatus(status);
            material.setUserGroups(userGroups);
            return material;
        }
    }
}
