package uk.gov.moj.cpp.hearing.command.plea;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Plea implements Serializable {
    final private UUID id;
    final private String value;
    final private LocalDate pleaDate;

    @JsonCreator
    public Plea(@JsonProperty("id") final UUID id,
                @JsonProperty("value") final String value,
                @JsonProperty("pleaDate") final LocalDate pleaDate) {
        this.id = id;
        this.pleaDate = pleaDate;
        this.value = value;

    }

    public UUID getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Plea)) { return false; }
        Plea plea = (Plea) o;
        return Objects.equals(getId(), plea.getId()) &&
                Objects.equals(getValue(), plea.getValue()) &&
                Objects.equals(getPleaDate(), plea.getPleaDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValue(), getPleaDate());
    }
}
