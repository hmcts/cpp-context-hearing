package uk.gov.moj.cpp.external.domain.listing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(value = Include.NON_NULL)
public class StatementOfOffence implements Serializable {

    private static final long serialVersionUID = 1L;
    private String title;
    private String legislation;

    public StatementOfOffence() {
    }

    @JsonCreator
    public StatementOfOffence(@JsonProperty(value = "title") final String title,
                              @JsonProperty(value = "legislation") final String legislation) {
        this.title = title;
        this.legislation = legislation;
    }

    public String getTitle() {
        return title;
    }

    public String getLegislation() {
        return legislation;
    }

    public StatementOfOffence setTitle(String title) {
        this.title = title;
        return this;
    }

    public StatementOfOffence setLegislation(String legislation) {
        this.legislation = legislation;
        return this;
    }

    public static StatementOfOffence statementOfOffence() {
        return new StatementOfOffence();
    }
}
