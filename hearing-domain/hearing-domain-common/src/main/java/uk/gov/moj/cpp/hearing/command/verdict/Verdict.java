package uk.gov.moj.cpp.hearing.command.verdict;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Verdict implements Serializable {
    final private UUID id;
    final private String value;

    @JsonCreator
    public Verdict(@JsonProperty("id") final UUID id,
                   @JsonProperty("value") final String value) {
        this.id = id;
        this.value = value;

    }

    public UUID getId() {
        return id;
    }

    public String getValue() {
        return value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Verdict)) { return false; }
        Verdict verdict = (Verdict) o;
        return Objects.equals(getId(), verdict.getId()) &&
                Objects.equals(getValue(), verdict.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValue());
    }
}
