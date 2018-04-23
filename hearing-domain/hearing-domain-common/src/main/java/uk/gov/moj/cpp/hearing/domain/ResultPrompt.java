package uk.gov.moj.cpp.hearing.domain;

import java.io.Serializable;
import java.util.Objects;

public class ResultPrompt implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String label;
    private final String value;

    public ResultPrompt(final String label, final String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResultPrompt that = (ResultPrompt) o;
        return Objects.equals(getLabel(), that.getLabel()) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabel(), getValue());
    }
}
