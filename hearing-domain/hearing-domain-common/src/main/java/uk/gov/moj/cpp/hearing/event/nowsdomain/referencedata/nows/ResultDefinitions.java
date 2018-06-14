package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.util.UUID;

public class ResultDefinitions {

    private UUID id;

    private Boolean mandatory;

    private Boolean primaryResult;

    private Integer sequence;

    public static ResultDefinitions resultDefinitions() {
        return new ResultDefinitions();
    }

    public UUID getId() {
        return this.id;
    }

    public ResultDefinitions setId(UUID id) {
        this.id = id;
        return this;
    }

    public Boolean getMandatory() {
        return this.mandatory;
    }

    public ResultDefinitions setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public Boolean getPrimaryResult() {
        return this.primaryResult;
    }

    public ResultDefinitions setPrimaryResult(Boolean primaryResult) {
        this.primaryResult = primaryResult;
        return this;
    }

    public Integer getSequence() {
        return this.sequence;
    }

    public ResultDefinitions setSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }
}
