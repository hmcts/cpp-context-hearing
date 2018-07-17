package uk.gov.moj.cpp.hearing.subscription.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;


public final class SubscriptionUploaded implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private String channel;

    private final Map<String, String> channelProperties;

    private final List<String> userGroups;

    private final String destination;

    private final List<UUID> courtCentreIds;

    private final List<UUID> nowTypeIds;

    public SubscriptionUploaded(@JsonProperty("id") final UUID id,
                                @JsonProperty("channel") final String channel,
                                @JsonProperty("channelProperties") final Map<String, String> properties,
                                @JsonProperty("userGroups") final List<String> userGroups,
                                @JsonProperty("destination") final String destination,
                                @JsonProperty("courtCentreIds") final List<UUID> courtCentreIds,
                                @JsonProperty("nowTypeIds") final List<UUID> nowTypeIds) {

        this.id = id;
        this.channel = channel;
        this.channelProperties = new HashMap<>(properties);
        this.userGroups = new ArrayList<>(userGroups);
        this.destination = destination;
        this.courtCentreIds = new ArrayList<>(courtCentreIds);
        this.nowTypeIds = new ArrayList<>(nowTypeIds);

    }

    public UUID getId() {
        return id;
    }

    public String getChannel() {
        return channel;
    }

    public Map<String, String> getChannelProperties() {
        return new HashMap<>(channelProperties);
    }

    public List<String> getUserGroups() {
        return new ArrayList<>(userGroups);
    }

    public String getDestination() {
        return destination;
    }

    public List<UUID> getCourtCentreIds() {
        return new ArrayList<>(courtCentreIds);
    }

    public List<UUID> getNowTypeIds() {
        return new ArrayList<>(nowTypeIds);
    }
}