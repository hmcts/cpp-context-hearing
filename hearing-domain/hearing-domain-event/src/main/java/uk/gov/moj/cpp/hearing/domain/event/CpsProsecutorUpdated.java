package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.cps-prosecutor-updated")
public class CpsProsecutorUpdated implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;
    private UUID prosecutionCaseId;
    private UUID prosecutionAuthorityId;
    private String prosecutionAuthorityCode;
    private String prosecutionAuthorityName;
    private String prosecutionAuthorityReference;
    private String caseURN;
    private Address address;

    public CpsProsecutorUpdated() {
    }

    @JsonCreator
    public CpsProsecutorUpdated(
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("prosecutionCaseId") UUID prosecutionCaseId,
            @JsonProperty("prosecutionAuthorityId") UUID prosecutionAuthorityId,
            @JsonProperty("prosecutionAuthorityCode") String prosecutionAuthorityCode,
            @JsonProperty("prosecutionAuthorityName") String prosecutionAuthorityName,
            @JsonProperty("prosecutionAuthorityReference") String prosecutionAuthorityReference,
            @JsonProperty("caseURN") String caseURN,
            @JsonProperty("address") Address address
    ) {
        super();
        this.hearingId = hearingId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.prosecutionAuthorityId = prosecutionAuthorityId;
        this.prosecutionAuthorityCode = prosecutionAuthorityCode;
        this.prosecutionAuthorityName = prosecutionAuthorityName;
        this.prosecutionAuthorityReference = prosecutionAuthorityReference;
        this.caseURN = caseURN;
        this.address = address;
    }

    public static CpsProsecutorUpdated cpsProsecutorUpdated() {
        return new CpsProsecutorUpdated();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public CpsProsecutorUpdated setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public CpsProsecutorUpdated setProsecutionCaseId(final UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public UUID getProsecutionAuthorityId() {
        return prosecutionAuthorityId;
    }

    public CpsProsecutorUpdated setProsecutionAuthorityId(final UUID prosecutionAuthorityId) {
        this.prosecutionAuthorityId = prosecutionAuthorityId;
        return this;
    }

    public String getProsecutionAuthorityCode() {
        return prosecutionAuthorityCode;
    }

    public CpsProsecutorUpdated setProsecutionAuthorityCode(final String prosecutionAuthorityCode) {
        this.prosecutionAuthorityCode = prosecutionAuthorityCode;
        return this;
    }

    public String getProsecutionAuthorityName() {
        return prosecutionAuthorityName;
    }

    public CpsProsecutorUpdated setProsecutionAuthorityName(final String prosecutionAuthorityName) {
        this.prosecutionAuthorityName = prosecutionAuthorityName;
        return this;
    }

    public String getProsecutionAuthorityReference() {
        return prosecutionAuthorityReference;
    }

    public CpsProsecutorUpdated setProsecutionAuthorityReference(final String prosecutionAuthorityReference) {
        this.prosecutionAuthorityReference = prosecutionAuthorityReference;
        return this;
    }

    public String getCaseURN() {
        return caseURN;
    }

    public CpsProsecutorUpdated setCaseURN(final String caseURN) {
        this.caseURN = caseURN;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public CpsProsecutorUpdated setAddress(final Address address) {
        this.address = address;
        return this;
    }

}
