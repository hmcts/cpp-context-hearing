package uk.gov.moj.cpp.hearing.command.plea;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Plea implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String value;
    private final LocalDate pleaDate;

    @JsonCreator
    public Plea(@JsonProperty("id") final UUID id,
                @JsonProperty("value") final String value,
                @JsonProperty("pleaDate") final LocalDate pleaDate) {
        this.id = id;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    public static Plea.Builder builder() {
        return new Plea.Builder();
    }

    public static Plea.Builder from(Plea plea) {
        return builder()
                .withId(plea.getId())
                .withValue(plea.getValue())
                .withPleaDate(plea.getPleaDate());
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

    public static final class Builder {

        private UUID id;
        private String value;
        private LocalDate pleaDate;

        private Builder() {

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

        public Plea.Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Plea.Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Plea.Builder withPleaDate(LocalDate pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }

        public Plea build() {
            return new Plea(this.id, this.value, this.pleaDate);
        }
    }
}
