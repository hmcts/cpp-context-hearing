
package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.List;

public class HearingDetailsResponse {

    private String hearingId;
    private String startDate;
    private String startTime;
    private String roomName;
    private String hearingType;
    private String courtCentreName;
    private Judge judge;
    private String roomId;
    private String courtCentreId;
    private Attendees attendees;
    private List<Case> cases = null;

    public String getHearingId() {
        return hearingId;
    }

    public void setHearingId(String hearingId) {
        this.hearingId = hearingId;
    }

    public HearingDetailsResponse withHearingId(String hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public HearingDetailsResponse withStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public HearingDetailsResponse withStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public HearingDetailsResponse withRoomName(String roomName) {
        this.roomName = roomName;
        return this;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    public HearingDetailsResponse withHearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public HearingDetailsResponse withCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public Judge getJudge() {
        return judge;
    }

    public void setJudge(Judge judge) {
        this.judge = judge;
    }

    public HearingDetailsResponse withJudge(Judge judge) {
        this.judge = judge;
        return this;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public HearingDetailsResponse withRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    public String getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public HearingDetailsResponse withCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    public Attendees getAttendees() {
        return attendees;
    }

    public void setAttendees(Attendees attendees) {
        this.attendees = attendees;
    }

    public HearingDetailsResponse withAttendees(Attendees attendees) {
        this.attendees = attendees;
        return this;
    }

    public List<Case> getCases() {
        return cases;
    }

    public void setCases(List<Case> cases) {
        this.cases = cases;
    }

    public HearingDetailsResponse withCases(List<Case> cases) {
        this.cases = cases;
        return this;
    }

    public static class Address {

        private String formattedAddress;
        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String postCode;

        public String getformattedAddress() {
            return formattedAddress;
        }

        public Address withformattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
            return this;
        }

        public String getAddress1() {
            return address1;
        }

        public Address withAddress1(String address1) {
            this.address1 = address1;
            return this;
        }

        public String getAddress2() {
            return address2;
        }

        public Address withAddress2(String address2) {
            this.address2 = address2;
            return this;
        }

        public String getAddress3() {
            return address3;
        }

        public Address withAddress3(String address3) {
            this.address3 = address3;
            return this;
        }

        public String getAddress4() {
            return address4;
        }

        public Address withAddress4(String address4) {
            this.address4 = address4;
            return this;
        }

        public String getPostCode() {
            return postCode;
        }

        public Address withPostCode(String postCode) {
            this.postCode = postCode;
            return this;
        }

    }

    public static class Attendees {

        private List<ProsecutionCounsel> prosecutionCounsels = null;
        private List<DefenceCounsel> defenceCounsels = null;

        public List<ProsecutionCounsel> getProsecutionCounsels() {
            return prosecutionCounsels;
        }

        public Attendees withProsecutionCounsels(List<ProsecutionCounsel> prosecutionCounsels) {
            this.prosecutionCounsels = prosecutionCounsels;
            return this;
        }

        public List<DefenceCounsel> getDefenceCounsels() {
            return defenceCounsels;
        }

        public Attendees withDefenceCounsels(List<DefenceCounsel> defenceCounsels) {
            this.defenceCounsels = defenceCounsels;
            return this;
        }
    }

    public static class Case {

        private String caseId;
        private String caseUrn;
        private List<Defendant> defendants = null;

        public String getCaseId() {
            return caseId;
        }

        public Case withCaseId(String caseId) {
            this.caseId = caseId;
            return this;
        }

        public String getCaseUrn() {
            return caseUrn;
        }

        public Case withCaseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public List<Defendant> getDefendants() {
            return defendants;
        }

        public Case withDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }
    }

    public static class DefenceCounsel {

        private String attendeeId;
        private String status;
        private String defendantId;
        private String title;
        private String firstName;
        private String lastName;

        public String getAttendeeId() {
            return attendeeId;
        }

        public DefenceCounsel withAttendeeId(String attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        public String getStatus() {
            return status;
        }

        public DefenceCounsel withStatus(String status) {
            this.status = status;
            return this;
        }

        public String getDefendantId() {
            return defendantId;
        }

        public DefenceCounsel withDefendantId(String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public DefenceCounsel withTitle(String title) {
            this.title = title;
            return this;
        }

        public String getFirstName() {
            return firstName;
        }

        public DefenceCounsel withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() {
            return lastName;
        }

        public DefenceCounsel withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
    }

    public static class Defendant {

        private String defendantId;
        private String personId;
        private String id;
        private String firstName;
        private String lastName;
        private String homeTelephone;
        private String mobile;
        private String fax;
        private String email;
        private Address address;
        private String dateOfBirth;
        private List<Offence> offences = null;

        public String getDefendantId() {
            return defendantId;
        }

        public Defendant withDefendantId(String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public String getPersonId() {
            return personId;
        }

        public Defendant withPersonId(String personId) {
            this.personId = personId;
            return this;
        }

        public String getId() {
            return id;
        }

        public Defendant withId(String id) {
            this.id = id;
            return this;
        }

        public String getFirstName() {
            return firstName;
        }

        public Defendant withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() {
            return lastName;
        }

        public Defendant withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public String getHomeTelephone() {
            return homeTelephone;
        }

        public Defendant withHomeTelephone(String homeTelephone) {
            this.homeTelephone = homeTelephone;
            return this;
        }

        public String getMobile() {
            return mobile;
        }

        public Defendant withMobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public String getFax() {
            return fax;
        }

        public Defendant withFax(String fax) {
            this.fax = fax;
            return this;
        }

        public String getEmail() {
            return email;
        }

        public Defendant withEmail(String email) {
            this.email = email;
            return this;
        }

        public Address getAddress() {
            return address;
        }

        public Defendant withAddress(Address address) {
            this.address = address;
            return this;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public Defendant withDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public List<Offence> getOffences() {
            return offences;
        }

        public Defendant withOffences(List<Offence> offences) {
            this.offences = offences;
            return this;
        }
    }

    public static class Judge {

        private String id;
        private String title;
        private String firstName;
        private String lastName;

        public String getId() {
            return id;
        }

        public Judge withId(String id) {
            this.id = id;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Judge withTitle(String title) {
            this.title = title;
            return this;
        }

        public String getFirstName() {
            return firstName;
        }

        public Judge withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() {
            return lastName;
        }

        public Judge withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
    }

    public static class Offence {

        private String id;
        private String wording;
        private Integer count;
        private String title;
        private String legislation;
        private Plea plea;
        private Verdict verdict;

        public String getId() {
            return id;
        }

        public Offence withId(String id) {
            this.id = id;
            return this;
        }

        public String getWording() {
            return wording;
        }

        public Offence withWording(String wording) {
            this.wording = wording;
            return this;
        }

        public Integer getCount() {
            return count;
        }

        public Offence withCount(Integer count) {
            this.count = count;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Offence withTitle(String title) {
            this.title = title;
            return this;
        }

        public String getLegislation() {
            return legislation;
        }

        public Offence withLegislation(String legislation) {
            this.legislation = legislation;
            return this;
        }

        public Plea getPlea() {
            return plea;
        }

        public Offence withPlea(Plea plea) {
            this.plea = plea;
            return this;
        }

        public Verdict getVerdict() {
            return verdict;
        }

        public Offence withVerdict(Verdict verdict) {
            this.verdict = verdict;
            return this;
        }
    }

    public static class Plea {

        private String pleaId;
        private String pleaDate;
        private String value;

        public String getPleaId() {
            return pleaId;
        }

        public Plea withPleaId(String pleaId) {
            this.pleaId = pleaId;
            return this;
        }

        public String getPleaDate() {
            return pleaDate;
        }

        public Plea withPleaDate(String pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }

        public String getValue() {
            return value;
        }

        public Plea withValue(String value) {
            this.value = value;
            return this;
        }
    }

    public static class ProsecutionCounsel {

        private String attendeeId;
        private String status;
        private String title;
        private String firstName;
        private String lastName;

        public String getAttendeeId() {
            return attendeeId;
        }

        public ProsecutionCounsel withAttendeeId(String attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        public String getStatus() {
            return status;
        }

        public ProsecutionCounsel withStatus(String status) {
            this.status = status;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public ProsecutionCounsel withTitle(String title) {
            this.title = title;
            return this;
        }

        public String getFirstName() {
            return firstName;
        }

        public ProsecutionCounsel withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() {
            return lastName;
        }

        public ProsecutionCounsel withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
    }

    public static class Value {

        private String id;
        private String category;
        private String code;
        private String description;

        public String getId() {
            return id;
        }

        public Value withId(String id) {
            this.id = id;
            return this;
        }

        public String getCategory() {
            return category;
        }

        public Value withCategory(String category) {
            this.category = category;
            return this;
        }

        public String getCode() {
            return code;
        }

        public Value withCode(String code) {
            this.code = code;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Value withDescription(String description) {
            this.description = description;
            return this;
        }
    }

    public static class  Verdict {

        private String verdictId;
        private String hearingId;
        private Value value;
        private String verdictDate;
        private Integer numberOfSplitJurors;
        private Integer numberOfJurors;
        private Boolean unanimous;

        public String getVerdictId() {
            return verdictId;
        }

        public Verdict withVerdictId(String verdictId) {
            this.verdictId = verdictId;
            return this;
        }

        public String getHearingId() {
            return hearingId;
        }

        public Verdict withHearingId(String hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Value getValue() {
            return value;
        }

        public Verdict withValue(Value value) {
            this.value = value;
            return this;
        }

        public String getVerdictDate() {
            return verdictDate;
        }

        public Verdict withVerdictDate(String verdictDate) {
            this.verdictDate = verdictDate;
            return this;
        }

        public Integer getNumberOfSplitJurors() {
            return numberOfSplitJurors;
        }

        public Verdict withNumberOfSplitJurors(Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
            return this;
        }

        public Integer getNumberOfJurors() {
            return numberOfJurors;
        }

        public Verdict withNumberOfJurors(Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
            return this;
        }

        public Boolean getUnanimous() {
            return unanimous;
        }

        public Verdict withUnanimous(Boolean unanimous) {
            this.unanimous = unanimous;
            return this;
        }
    }
}