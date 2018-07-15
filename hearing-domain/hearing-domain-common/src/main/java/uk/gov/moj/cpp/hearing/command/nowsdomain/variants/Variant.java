package uk.gov.moj.cpp.hearing.command.nowsdomain.variants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Variant implements Serializable {

    private VariantKey key;

    private VariantValue value;

    private LocalDate referenceDate;

    public static Variant variant() {
        return new Variant();
    }

    public VariantKey getKey() {
        return this.key;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public Variant setKey(final VariantKey key) {
        this.key = key;
        return this;
    }

    public VariantValue getValue() {
        return this.value;
    }

    public Variant setValue(final VariantValue value) {
        this.value = value;
        return this;
    }

    public Variant setReferenceDate(final LocalDate referenceDate) {
        this.referenceDate = referenceDate;
        return this;
    }
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Variant variant = (Variant) o;
        return Objects.equals(key, variant.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key);
    }
}
