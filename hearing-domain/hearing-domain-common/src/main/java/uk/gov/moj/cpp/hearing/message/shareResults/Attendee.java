package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Attendee {

    private UUID personId;

    private String firstName;

    private String lastName;

    private String type;

    private String title;

    private List<UUID> defendantIds;

    private String status;

    private List<UUID> caseIds;

    public static Attendee attendee() {
        return new Attendee();
    }

    public UUID getPersonId() {
        return personId;
    }

    public Attendee setPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Attendee setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Attendee setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getType() {
        return type;
    }

    public Attendee setType(String type) {
        this.type = type;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Attendee setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<UUID> getDefendantIds() {
        return defendantIds;
    }

    public Attendee setDefendantIds(List<UUID> defendantIds) {
        this.defendantIds = new ArrayList<>(defendantIds);
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Attendee setStatus(String status) {
        this.status = status;
        return this;
    }

    public List<UUID> getCaseIds() {
        return caseIds;
    }

    public Attendee setCaseIds(List<UUID> caseIds) {
        this.caseIds = new ArrayList<>(caseIds);
        return this;
    }
}
