
package uk.gov.moj.cpp.hearing.query.view.response.nowresponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class Material {

    @JsonProperty("id")
    private String id;
    @JsonProperty("language")
    private String language;
    @JsonProperty("status")
    private String status;
    @JsonProperty("userGroups")
    private List<String> userGroups = new ArrayList<>();

    @JsonProperty("nowResult")
    private List<NowResult> nowResult = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public List<NowResult> getNowResult() {
        return nowResult;
    }

    public void setNowResult(List<NowResult> nowResult) {
        this.nowResult = nowResult;
    }


    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        private String id;
        private String language;
        private String status;
        private List<String> userGroups = new ArrayList<>();
        private List<NowResult> nowResult = new ArrayList<>();

        private Builder() {
        }



        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
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

        public Builder withNowResult(List<NowResult> nowResult) {
            this.nowResult = nowResult;
            return this;
        }

        public Material build() {
            Material material = new Material();
            material.setId(id);
            material.setLanguage(language);
            material.setStatus(status);
            material.setUserGroups(userGroups);
            material.setNowResult(nowResult);
            return material;
        }
    }
}
