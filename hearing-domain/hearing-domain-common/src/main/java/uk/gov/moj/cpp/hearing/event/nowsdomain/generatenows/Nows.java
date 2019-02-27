package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

//TODO GPE-6313 remove
@SuppressWarnings({"squid:S1135"})
public class Nows implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID nowsTypeId;

    private String nowsTemplateName;

    private UUID defendantId;

    private List<Material> materials;

    @JsonIgnore
    private LocalDate referenceDate;

    public static Nows nows() {
        return new Nows();
    }

    public UUID getId() {
        return this.id;
    }

    public Nows setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getNowsTypeId() {
        return this.nowsTypeId;
    }

    public Nows setNowsTypeId(final UUID nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
        return this;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public Nows setReferenceDate(final LocalDate referenceDate) {
        this.referenceDate = referenceDate;
        return this;
    }

    public String getNowsTemplateName() {
        return this.nowsTemplateName;
    }

    public Nows setNowsTemplateName(final String nowsTemplateName) {
        this.nowsTemplateName = nowsTemplateName;
        return this;
    }

    public UUID getDefendantId() {
        return this.defendantId;
    }

    public Nows setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public List<Material> getMaterials() {
        return this.materials;
    }

    public Nows setMaterials(final List<Material> materials) {
        this.materials = new ArrayList<>(materials);
        return this;
    }

}
