package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class VerdictType {

    @Column(name = "verdict_category")
    private String verdictCategory;

    @Column(name = "verdict_category_type")
    private String verdictCategoryType;

    @Column(name = "verdict_type_id")
    private UUID verdictTypeId;

    @Column(name = "verdict_description")
    private String description;

    @Column(name = "verdict_sequence")
    private Integer sequence;

    public String getVerdictCategory() {
        return verdictCategory;
    }

    public void setVerdictCategory(String verdictCategory) {
        this.verdictCategory = verdictCategory;
    }

    public String getVerdictCategoryType() {
        return verdictCategoryType;
    }

    public void setVerdictCategoryType(String verdictCategoryType) {
        this.verdictCategoryType = verdictCategoryType;
    }

    public UUID getVerdictTypeId() {
        return verdictTypeId;
    }

    public void setVerdictTypeId(UUID verdictTypeId) {
        this.verdictTypeId = verdictTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
}




