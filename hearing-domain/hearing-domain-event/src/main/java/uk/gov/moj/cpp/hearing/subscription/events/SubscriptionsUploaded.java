package uk.gov.moj.cpp.hearing.subscription.events;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.subscriptions-uploaded")
public class SubscriptionsUploaded implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private List<SubscriptionUploaded> subscriptions;

    private String referenceDate;

    @JsonCreator
    public SubscriptionsUploaded(@JsonProperty("id") final UUID id,
                                 @JsonProperty("subscriptions") final List<SubscriptionUploaded> subscriptions,
                                 @JsonProperty("referenceDate") final String referenceDate) {
        this.id = id;
        this.subscriptions = new ArrayList<>(subscriptions);
        this.referenceDate = referenceDate;
    }

    public UUID getId() {
        return id;
    }

    public List<SubscriptionUploaded> getSubscriptions() {
        return new ArrayList<>(subscriptions);
    }

    public String getReferenceDate() {
        return referenceDate;
    }
}
