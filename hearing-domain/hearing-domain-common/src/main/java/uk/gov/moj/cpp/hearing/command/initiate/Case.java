package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Case implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID caseId;
    private String urn;

    public Case() {
    }

    @JsonCreator
    public Case(@JsonProperty("caseId") UUID caseId, @JsonProperty("urn") String urn) {
        this.caseId = caseId;
        this.urn = urn;
    }

    public static Case legalCase() {
        return new Case();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public Case setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public String getUrn() {
        return urn;
    }

    public Case setUrn(String urn) {
        this.urn = urn;
        return this;
    }
}
