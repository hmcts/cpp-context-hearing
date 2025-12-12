package uk.gov.moj.cpp.hearing.persist.entity.not;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "not_subscription")
public class Subscription {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "channel")
    private String channel;

    @Column(name = "destination")
    private String destination;

    @ElementCollection
    @CollectionTable(name = "not_subscription_property", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "value")
    @MapKeyColumn(name = "key")
    private Map<String, String> channelProperties = new HashMap<>();

    @ElementCollection
    @CollectionTable(
            name = "not_subscription_usergroup",
            joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Column(name = "user_group")
    private List<String> userGroups = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "not_subscription_courtcentre",
            joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Column(name = "court_centre_id")
    private List<UUID> courtCentreIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "not_subscription_nowtype",
            joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Column(name = "now_type_id")
    private List<UUID> nowTypeIds = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Map<String, String> getChannelProperties() {
        return channelProperties;
    }

    public void setChannelProperties(Map<String, String> channelProperties) {
        this.channelProperties = channelProperties;
    }

    public List<String> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<UUID> getCourtCentreIds() {
        return courtCentreIds;
    }

    public void setCourtCentreIds(List<UUID> courtCentreIds) {
        this.courtCentreIds = courtCentreIds;
    }

    public List<UUID> getNowTypeIds() {
        return nowTypeIds;
    }

    public void setNowTypeIds(List<UUID> nowTypeIds) {
        this.nowTypeIds = nowTypeIds;
    }
}