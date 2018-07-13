package uk.gov.moj.cpp.hearing.command.nowsdomain.variants;

import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class VariantValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID materialId;

    private List<ResultLineReference> resultLines;

    private VariantStatus status;

    public static VariantValue variantValue() {
        return new VariantValue();
    }

    public UUID getMaterialId() {
        return this.materialId;
    }

    public VariantValue setMaterialId(UUID materialId) {
        this.materialId = materialId;
        return this;
    }

    public List<ResultLineReference> getResultLines() {
        return this.resultLines;
    }

    public VariantValue setResultLines(List<ResultLineReference> resultLines) {
        this.resultLines = resultLines;
        return this;
    }

    public VariantStatus getStatus() {
        return status;
    }

    public VariantValue setStatus(final VariantStatus status) {
        this.status = status;
        return this;
    }
}
