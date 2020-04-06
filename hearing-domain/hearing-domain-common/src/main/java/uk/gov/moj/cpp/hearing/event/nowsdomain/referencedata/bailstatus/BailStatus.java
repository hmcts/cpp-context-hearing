package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus;

import java.util.UUID;

public class BailStatus {

    private UUID id;
    private String statusCode;
    private String statusDescription;
    private Integer statusRanking;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(final String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Integer getStatusRanking() {
        return statusRanking;
    }

    public void setStatusRanking(final Integer statusRanking) {
        this.statusRanking = statusRanking;
    }

}
