package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class Ethnicity {

    @Column(name = "observed_ethnicity_code")
    private String observedEthnicityCode;

    @Column(name = "observed_ethnicity_description")
    private String observedEthnicityDescription;

    @Column(name = "observed_ethnicity_id")
    private UUID observedEthnicityId;

    @Column(name = "self_defined_ethnicity_code")
    private String selfDefinedEthnicityCode;

    @Column(name = "self_defined_ethnicity_description")
    private String selfDefinedEthnicityDescription;

    @Column(name = "self_defined_ethnicity_id")
    private UUID selfDefinedEthnicityId;

    public String getObservedEthnicityCode() {
        return observedEthnicityCode;
    }

    public UUID getObservedEthnicityId() {
        return observedEthnicityId;
    }

    public String getSelfDefinedEthnicityCode() {
        return selfDefinedEthnicityCode;
    }

    public UUID getSelfDefinedEthnicityId() {
        return selfDefinedEthnicityId;
    }

    public void setObservedEthnicityCode(String observedEthnicityCode) {
        this.observedEthnicityCode = observedEthnicityCode;
    }

    public void setObservedEthnicityId(UUID observedEthnicityId) {
        this.observedEthnicityId = observedEthnicityId;
    }

    public void setSelfDefinedEthnicityCode(String selfDefinedEthnicityCode) {
        this.selfDefinedEthnicityCode = selfDefinedEthnicityCode;
    }

    public void setSelfDefinedEthnicityId(UUID selfDefinedEthnicityId) {
        this.selfDefinedEthnicityId = selfDefinedEthnicityId;
    }

    public String getObservedEthnicityDescription() {
        return observedEthnicityDescription;
    }

    public void setObservedEthnicityDescription(final String observedEthnicityDescription) {
        this.observedEthnicityDescription = observedEthnicityDescription;
    }

    public String getSelfDefinedEthnicityDescription() {
        return selfDefinedEthnicityDescription;
    }

    public void setSelfDefinedEthnicityDescription(final String selfDefinedEthnicityDescription) {
        this.selfDefinedEthnicityDescription = selfDefinedEthnicityDescription;
    }
}
