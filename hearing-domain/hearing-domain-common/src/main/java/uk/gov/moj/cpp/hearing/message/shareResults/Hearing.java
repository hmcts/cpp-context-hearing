package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Hearing {

    private UUID id;
    private String hearingType;
    private ZonedDateTime startDateTime;
    private List<ZonedDateTime> hearingDates;
    private CourtCentre courtCentre;
    private List<Attendee> attendees;
    private List<Defendant> defendants;

    public UUID getId() {
        return id;
    }

    public String getHearingType() {
        return hearingType;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public List<ZonedDateTime> getHearingDates() {
        return hearingDates;
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public Hearing setHearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public Hearing setStartDateTime(ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public Hearing setHearingDates(List<ZonedDateTime> hearingDates) {
        this.hearingDates = hearingDates;
        return this;
    }

    public Hearing setCourtCentre(CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public Hearing setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
        return this;
    }

    public Hearing setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
        return this;
    }

    public static Hearing hearing() {
        return new Hearing();
    }
}
