package uk.gov.moj.cpp.external.domain.listing;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class StatementOfOffence implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String title;
    private final String legislation;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatementOfOffence that = (StatementOfOffence) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(legislation, that.legislation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, legislation);
    }
}
