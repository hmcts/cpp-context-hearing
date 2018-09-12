package uk.gov.moj.cpp.hearing.nows.events;

import uk.gov.justice.json.schemas.core.CourtCentre;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hearing implements Serializable {
    private static final long serialVersionUID = -791406733718995646L;

    private String id;
    private String hearingType;
    private String startDateTime;
    private List<String> hearingDates = new ArrayList<String>();
    private uk.gov.justice.json.schemas.core.CourtCentre courtCentre;
    private List<Attendee> attendees = new ArrayList<Attendee>();
    private List<Defendant> defendants = new ArrayList<Defendant>();
    private List<SharedResultLine> sharedResultLines = new ArrayList<SharedResultLine>();
    private List<Now> nows = new ArrayList<Now>();
    private List<NowType> nowTypes = new ArrayList<NowType>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public List<String> getHearingDates() {
        return hearingDates;
    }

    public void setHearingDates(List<String> hearingDates) {
        this.hearingDates = hearingDates;
    }

    public uk.gov.justice.json.schemas.core.CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public void setCourtCentre(uk.gov.justice.json.schemas.core.CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public void setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
    }

    public List<SharedResultLine> getSharedResultLines() {
        return sharedResultLines;
    }

    public void setSharedResultLines(List<SharedResultLine> sharedResultLines) {
        this.sharedResultLines = sharedResultLines;
    }

    public List<Now> getNows() {
        return nows;
    }

    public void setNows(List<Now> nows) {
        this.nows = nows;
    }

    public List<NowType> getNowTypes() {
        return nowTypes;
    }

    public void setNowTypes(List<NowType> nowTypes) {
        this.nowTypes = nowTypes;
    }


    public static final class HearingBuilder {
        private String id;
        private String hearingType;
        private String startDateTime;
        private List<String> hearingDates = new ArrayList<String>();
        private uk.gov.justice.json.schemas.core.CourtCentre courtCentre;
        private List<Attendee> attendees = new ArrayList<Attendee>();
        private List<Defendant> defendants = new ArrayList<Defendant>();
        private List<SharedResultLine> sharedResultLines = new ArrayList<SharedResultLine>();
        private List<Now> nows = new ArrayList<Now>();
        private List<NowType> nowTypes = new ArrayList<NowType>();

        private HearingBuilder() {
        }

        public static HearingBuilder builder() {
            return new HearingBuilder();
        }

        public HearingBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public HearingBuilder withHearingType(String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public HearingBuilder withStartDateTime(String startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public HearingBuilder withHearingDates(List<String> hearingDates) {
            this.hearingDates = hearingDates;
            return this;
        }

        public HearingBuilder withCourtCentre(CourtCentre courtCentre) {
            this.courtCentre = courtCentre;
            return this;
        }

        public HearingBuilder withAttendees(List<Attendee> attendees) {
            this.attendees = attendees;
            return this;
        }

        public HearingBuilder withDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

        public HearingBuilder withSharedResultLines(List<SharedResultLine> sharedResultLines) {
            this.sharedResultLines = sharedResultLines;
            return this;
        }

        public HearingBuilder withNows(List<Now> nows) {
            this.nows = nows;
            return this;
        }

        public HearingBuilder withNowTypes(List<NowType> nowTypes) {
            this.nowTypes = nowTypes;
            return this;
        }

        public Hearing build() {
            Hearing hearing = new Hearing();
            hearing.setId(id);
            hearing.setHearingType(hearingType);
            hearing.setStartDateTime(startDateTime);
            hearing.setHearingDates(hearingDates);
            hearing.setCourtCentre(courtCentre);
            hearing.setAttendees(attendees);
            hearing.setDefendants(defendants);
            hearing.setSharedResultLines(sharedResultLines);
            hearing.setNows(nows);
            hearing.setNowTypes(nowTypes);
            return hearing;
        }
    }
}
