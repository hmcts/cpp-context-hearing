package uk.gov.moj.cpp.hearing.command.verdict;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VerdictValue implements Serializable {
    final private UUID id;
    final private String category;
    final private String code;
    final private String description;

    @JsonCreator
    public VerdictValue(@JsonProperty("id") final UUID id,
                        @JsonProperty("category") final String category,
                        @JsonProperty("code") final String code,
                        @JsonProperty("description") final String description) {
        this.id = id;
        this.category = category;
        this.code = code;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerdictValue)) {
            return false;
        }
        VerdictValue verdict = (VerdictValue) o;
        return Objects.equals(getId(), verdict.getId()) &&
                Objects.equals(getCategory(), verdict.getCategory()) &&
                Objects.equals(getCode(), verdict.getCode()) &&
                Objects.equals(getDescription(), verdict.getDescription());

    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCategory(), getCode(), getDescription());
    }
}
