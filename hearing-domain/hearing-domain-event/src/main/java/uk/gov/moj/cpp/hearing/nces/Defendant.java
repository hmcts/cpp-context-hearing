package uk.gov.moj.cpp.hearing.nces;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"squid:S1948"})
public class Defendant implements Serializable {
    private static final long serialVersionUID = -4485480031610795301L;

    private final LocalDate dateOfBirth;

    private final String name;

    private final Map<String, Object> additionalProperties;

    public Defendant(final LocalDate dateOfBirth, final String name, final Map<String, Object> additionalProperties) {
        this.dateOfBirth = dateOfBirth;
        this.name = name;
        this.additionalProperties = additionalProperties;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public static Builder defendant() {
        return new uk.gov.moj.cpp.hearing.nces.Defendant.Builder();
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperty(final String name, final Object value) {
        additionalProperties.put(name, value);
    }

    public static class Builder {
        private LocalDate dateOfBirth;

        private String name;

        private final Map<String, Object> additionalProperties = new HashMap<>();

        public Builder withDateOfBirth(final LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withAdditionalProperty(final String name, final Object value) {
            additionalProperties.put(name, value);
            return this;
        }

        public Defendant build() {
            return new uk.gov.moj.cpp.hearing.nces.Defendant(dateOfBirth, name, additionalProperties);
        }
    }
}
