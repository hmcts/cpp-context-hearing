package uk.gov.moj.cpp.hearing.domain;

import java.util.UUID;

public class OffenceBailStatus {
    private UUID offenceId;
    private UUID bailStatusId;
    private String bailStatusCode;
    private String bailStatusDesc;

    public OffenceBailStatus(final UUID offenceId, final UUID bailStatusId, final String bailStatusCode, final String bailStatusDesc) {
        this.offenceId = offenceId;
        this.bailStatusId = bailStatusId;
        this.bailStatusCode = bailStatusCode;
        this.bailStatusDesc = bailStatusDesc;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public void setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
    }

    public UUID getBailStatusId() {
        return bailStatusId;
    }

    public void setBailStatusId(final UUID bailStatusId) {
        this.bailStatusId = bailStatusId;
    }

    public String getBailStatusCode() {
        return bailStatusCode;
    }

    public void setBailStatusCode(final String bailStatusCode) {
        this.bailStatusCode = bailStatusCode;
    }

    public String getBailStatusDesc() {
        return bailStatusDesc;
    }

    public void setBailStatusDesc(final String bailStatusDesc) {
        this.bailStatusDesc = bailStatusDesc;
    }
}