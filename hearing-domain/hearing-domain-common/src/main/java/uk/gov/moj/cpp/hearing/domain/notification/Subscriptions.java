package uk.gov.moj.cpp.hearing.domain.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@SuppressWarnings({"squid:S1700"})
public class Subscriptions {

    @JsonProperty("subscriptions")
    private List<Subscription> subscriptions;

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
