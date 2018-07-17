package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.Collections.unmodifiableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.newdefence-counsel-added")
public class DefenceCounselUpsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID personId;
    private final UUID attendeeId;
    private final List<UUID> defendantIds;
    private final UUID hearingId;
    private final String status;
    private final String title;
    private final String firstName;
    private final String lastName;

    @JsonCreator
    protected DefenceCounselUpsert(@JsonProperty("personId") final UUID personId,
            @JsonProperty("attendeeId") final UUID attendeeId, 
            @JsonProperty("defendantIds") final List<UUID> defendantIds, 
            @JsonProperty("hearingId") final UUID hearingId, 
            @JsonProperty("status") final String status,
            @JsonProperty("title") final String title, 
            @JsonProperty("firstName") final String firstName, 
            @JsonProperty("lastName") final String lastName) {
        this.personId = personId;
        this.attendeeId = attendeeId;
        this.defendantIds = Collections.unmodifiableList(Optional.ofNullable(defendantIds).orElseGet(ArrayList::new));
        this.hearingId = hearingId;
        this.status = status;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @JsonIgnore
    private DefenceCounselUpsert(Builder builder) {
        this.attendeeId = builder.attendeeId;
        this.hearingId = builder.hearingId;
        this.status = builder.status;
        this.defendantIds = Collections.unmodifiableList(Optional.ofNullable(builder.defendantIds).orElseGet(ArrayList::new));
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.title = builder.title;
        this.personId = builder.personId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public List<UUID> getDefendantIds() {
        return unmodifiableList(defendantIds);
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static class Builder {
        private UUID personId;
        private UUID attendeeId;
        private List<UUID> defendantIds;
        private UUID hearingId;
        private String status;
        private String title;
        private String firstName;
        private String lastName;

        public Builder withPersonId(UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withAttendeeId(UUID attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        public Builder withDefendantIds(List<UUID> defendantIds) {
            this.defendantIds = new ArrayList<>(defendantIds);
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public DefenceCounselUpsert build() {
            return new DefenceCounselUpsert(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}

