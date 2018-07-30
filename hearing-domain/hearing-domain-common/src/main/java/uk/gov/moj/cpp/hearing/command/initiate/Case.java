package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class Case implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID caseId;
    private String urn;

    public Case(){}

    @JsonCreator
    public Case(@JsonProperty("caseId") UUID caseId, @JsonProperty("urn") String urn) {
        this.caseId = caseId;
        this.urn = urn;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }

    public Case setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public Case setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    public static Case legalCase(){
        return new Case();
    }
}
