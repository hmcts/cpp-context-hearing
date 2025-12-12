package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel;

import java.util.UUID;

@SuppressWarnings({"squid:S2384"})
public class AlcoholLevelMethod {

    private UUID id;

    private Integer seqNo;

    private String methodCode;

    private String methodDescription;

    public AlcoholLevelMethod() {
    }

    public AlcoholLevelMethod(final UUID id, final Integer seqNo, final String methodCode, final String methodDescription) {
        this.id = id;
        this.seqNo = seqNo;
        this.methodCode = methodCode;
        this.methodDescription = methodDescription;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(final Integer seqNo) {
        this.seqNo = seqNo;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(final String methodCode) {
        this.methodCode = methodCode;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public void setMethodDescription(final String methodDescription) {
        this.methodDescription = methodDescription;
    }
}
