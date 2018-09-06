package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class VerdictType {

    @Column(name = "verdict_category")
    private String verdictCategory;

    @Column(name = "verdict_category_type")
    private String verdictCategoryType;

    @Column(name = "verdict_type_id")
    private UUID verdictTypeId;

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
}




