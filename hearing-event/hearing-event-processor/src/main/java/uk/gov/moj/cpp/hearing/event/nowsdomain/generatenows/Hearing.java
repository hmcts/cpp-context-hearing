package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Hearing {

    private java.util.UUID id;

    private String hearingType;

    private String startDateTime;

    private java.util.List<String> hearingDates;

    private CourtCentre courtCentre;

    private java.util.List<Attendees> attendees;

    private java.util.List<Defendants> defendants;

    private java.util.List<SharedResultLines> sharedResultLines;

    private java.util.List<Nows> nows;

    private java.util.List<NowTypes> nowTypes;

    public static Hearing hearing() {
        return new Hearing();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Hearing setId(java.util.UUID id) {
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

    public java.util.List<String> getHearingDates() {
        return this.hearingDates;
    }

    public Hearing setHearingDates(java.util.List<String> hearingDates) {
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

    public java.util.List<Attendees> getAttendees() {
        return this.attendees;
    }

    public Hearing setAttendees(java.util.List<Attendees> attendees) {
        this.attendees = attendees;
        return this;
    }

    public java.util.List<Defendants> getDefendants() {
        return this.defendants;
    }

    public Hearing setDefendants(java.util.List<Defendants> defendants) {
        this.defendants = defendants;
        return this;
    }

    public java.util.List<SharedResultLines> getSharedResultLines() {
        return this.sharedResultLines;
    }

    public Hearing setSharedResultLines(java.util.List<SharedResultLines> sharedResultLines) {
        this.sharedResultLines = sharedResultLines;
        return this;
    }

    public java.util.List<Nows> getNows() {
        return this.nows;
    }

    public Hearing setNows(java.util.List<Nows> nows) {
        this.nows = nows;
        return this;
    }

    public java.util.List<NowTypes> getNowTypes() {
        return this.nowTypes;
    }

    public Hearing setNowTypes(java.util.List<NowTypes> nowTypes) {
        this.nowTypes = nowTypes;
        return this;
    }
}
