package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import uk.gov.justice.json.schemas.core.CourtCentre;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Hearing implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String hearingType;

    private String startDateTime;

    private List<ZonedDateTime> hearingDates;

    private uk.gov.justice.json.schemas.core.CourtCentre courtCentre;

    private List<Attendees> attendees;

    private List<Defendants> defendants;

    private List<SharedResultLines> sharedResultLines;

    public static Hearing hearing() {
        return new Hearing();
    }

    public UUID getId() {
        return this.id;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getHearingType() {
        return this.hearingType;
    }

    public Hearing setHearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public String getStartDateTime() {
        return this.startDateTime;
    }

    public Hearing setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public List<ZonedDateTime> getHearingDates() {
        return this.hearingDates;
    }

    public Hearing setHearingDates(List<ZonedDateTime> hearingDates) {
        this.hearingDates = new ArrayList<>(hearingDates);
        return this;
    }

    public CourtCentre getCourtCentre() {
        return this.courtCentre;
    }

    public Hearing setCourtCentre(uk.gov.justice.json.schemas.core.CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public List<Attendees> getAttendees() {
        return this.attendees;
    }

    public Hearing setAttendees(List<Attendees> attendees) {
        this.attendees = new ArrayList<>(attendees);
        return this;
    }

    public List<Defendants> getDefendants() {
        return this.defendants;
    }

    public Hearing setDefendants(List<Defendants> defendants) {
        this.defendants = new ArrayList<>(defendants);
        return this;
    }

    public List<SharedResultLines> getSharedResultLines() {
        return this.sharedResultLines;
    }

    public Hearing setSharedResultLines(List<SharedResultLines> sharedResultLines) {
        this.sharedResultLines = new ArrayList<>(sharedResultLines);
        return this;
    }
}
