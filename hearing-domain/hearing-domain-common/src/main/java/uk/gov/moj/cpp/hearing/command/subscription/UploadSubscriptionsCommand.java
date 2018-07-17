package uk.gov.moj.cpp.hearing.command.subscription;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadSubscriptionsCommand {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("subscriptions")
    private List<UploadSubscriptionCommand> subscriptions;

    @JsonProperty("referenceDate")
    private String referenceDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(String referenceDate) {
        this.referenceDate = referenceDate;
    }

    public List<UploadSubscriptionCommand> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<UploadSubscriptionCommand> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
