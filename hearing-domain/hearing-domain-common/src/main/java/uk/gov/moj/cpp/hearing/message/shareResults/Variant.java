package uk.gov.moj.cpp.hearing.message.shareResults;

import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;

import java.util.UUID;

public class Variant {

    private VariantKey key;

    private UUID materialId;

    private String templateName;

    private String description;

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

    public UUID getMaterialId() {
        return materialId;
    }

    public Variant setMaterialId(final UUID materialId) {
        this.materialId = materialId;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Variant setTemplateName(final String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Variant setDescription(final String description) {
        this.description = description;
        return this;
    }
}
