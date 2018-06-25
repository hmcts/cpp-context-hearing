package uk.gov.moj.cpp.hearing.command.nowsdomain.variants;

import java.io.Serializable;
import java.util.Objects;


public class Variant implements Serializable {

    private VariantKey key;

    private VariantValue value;

    public static Variant variant() {
        return new Variant();
    }

    public VariantKey getKey() {
        return this.key;
    }

    public Variant setKey(VariantKey key) {
        this.key = key;
        return this;
    }

    public VariantValue getValue() {
        return this.value;
    }

    public Variant setValue(VariantValue value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Variant variant = (Variant) o;
        return Objects.equals(key, variant.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key);
    }
}
