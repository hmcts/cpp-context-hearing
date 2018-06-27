package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Hearing implements Serializable {

    private UUID id;

    private String hearingType;

    private String startDateTime;

    private List<String> hearingDates;

    private CourtCentre courtCentre;

    private List<Attendees> attendees;

    private List<Defendants> defendants;

    private List<SharedResultLines> sharedResultLines;

    private List<Nows> nows;

    private List<NowTypes> nowTypes;

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

    public List<String> getHearingDates() {
        return this.hearingDates;
    }

    public Hearing setHearingDates(List<String> hearingDates) {
        this.hearingDates = hearingDates;
        return this;
    }

    public CourtCentre getCourtCentre() {
        return this.courtCentre;
    }

    public Hearing setCourtCentre(CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public List<Attendees> getAttendees() {
        return this.attendees;
    }

    public Hearing setAttendees(List<Attendees> attendees) {
        this.attendees = attendees;
        return this;
    }

    public List<Defendants> getDefendants() {
        return this.defendants;
    }

    public Hearing setDefendants(List<Defendants> defendants) {
        this.defendants = defendants;
        return this;
    }

    public List<SharedResultLines> getSharedResultLines() {
        return this.sharedResultLines;
    }

    public Hearing setSharedResultLines(List<SharedResultLines> sharedResultLines) {
        this.sharedResultLines = sharedResultLines;
        return this;
    }

    public List<Nows> getNows() {
        return this.nows;
    }

    public Hearing setNows(List<Nows> nows) {
        this.nows = nows;
        return this;
    }

    public List<NowTypes> getNowTypes() {
        return this.nowTypes;
    }

    public Hearing setNowTypes(List<NowTypes> nowTypes) {
        this.nowTypes = nowTypes;
        return this;
    }
}
