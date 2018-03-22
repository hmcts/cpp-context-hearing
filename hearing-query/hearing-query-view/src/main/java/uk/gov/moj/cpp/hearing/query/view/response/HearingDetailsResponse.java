
package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hearingId",
    "startDate",
    "startTime",
    "roomName",
    "hearingType",
    "courtCentreName",
    "judge",
    "roomId",
    "courtCentreId",
    "attendees",
    "cases"
})
public class HearingDetailsResponse {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hearingId")
    private String hearingId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startDate")
    private String startDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    private String startTime;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roomName")
    private String roomName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hearingType")
    private String hearingType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("courtCentreName")
    private String courtCentreName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("judge")
    private Judge judge;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roomId")
    private String roomId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("courtCentreId")
    private String courtCentreId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("attendees")
    private Attendees attendees;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("cases")
    private List<Case> cases = null;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hearingId")
    public String getHearingId() {
        return hearingId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hearingId")
    public void setHearingId(String hearingId) {
        this.hearingId = hearingId;
    }

    public HearingDetailsResponse withHearingId(String hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public HearingDetailsResponse withStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    public String getStartTime() {
        return startTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public HearingDetailsResponse withStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roomName")
    public String getRoomName() {
        return roomName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roomName")
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public HearingDetailsResponse withRoomName(String roomName) {
        this.roomName = roomName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hearingType")
    public String getHearingType() {
        return hearingType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hearingType")
    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    public HearingDetailsResponse withHearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("courtCentreName")
    public String getCourtCentreName() {
        return courtCentreName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("courtCentreName")
    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public HearingDetailsResponse withCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("judge")
    public Judge getJudge() {
        return judge;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("judge")
    public void setJudge(Judge judge) {
        this.judge = judge;
    }

    public HearingDetailsResponse withJudge(Judge judge) {
        this.judge = judge;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roomId")
    public String getRoomId() {
        return roomId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roomId")
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public HearingDetailsResponse withRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("courtCentreId")
    public String getCourtCentreId() {
        return courtCentreId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("courtCentreId")
    public void setCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public HearingDetailsResponse withCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("attendees")
    public Attendees getAttendees() {
        return attendees;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("attendees")
    public void setAttendees(Attendees attendees) {
        this.attendees = attendees;
    }

    public HearingDetailsResponse withAttendees(Attendees attendees) {
        this.attendees = attendees;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("cases")
    public List<Case> getCases() {
        return cases;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("cases")
    public void setCases(List<Case> cases) {
        this.cases = cases;
    }

    public HearingDetailsResponse withCases(List<Case> cases) {
        this.cases = cases;
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "formattedAddress",
        "address1",
        "address2",
        "address3",
        "address4",
        "postCode"
    })
    public static class Address {

        @JsonProperty("formattedAddress")
        private String formattedAddress;
        @JsonProperty("address1")
        private String address1;
        @JsonProperty("address2")
        private String address2;
        @JsonProperty("address3")
        private String address3;
        @JsonProperty("address4")
        private String address4;
        @JsonProperty("postCode")
        private String postCode;

        @JsonProperty("formattedAddress")
        public String getformattedAddress() {
            return formattedAddress;
        }

        @JsonProperty("formattedAddress")
        public void setformattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public Address withformattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
            return this;
        }

        @JsonProperty("address1")
        public String getAddress1() {
            return address1;
        }

        @JsonProperty("address1")
        public void setAddress1(String address1) {
            this.address1 = address1;
        }

        public Address withAddress1(String address1) {
            this.address1 = address1;
            return this;
        }

        @JsonProperty("address2")
        public String getAddress2() {
            return address2;
        }

        @JsonProperty("address2")
        public void setAddress2(String address2) {
            this.address2 = address2;
        }

        public Address withAddress2(String address2) {
            this.address2 = address2;
            return this;
        }

        @JsonProperty("address3")
        public String getAddress3() {
            return address3;
        }

        @JsonProperty("address3")
        public void setAddress3(String address3) {
            this.address3 = address3;
        }

        public Address withAddress3(String address3) {
            this.address3 = address3;
            return this;
        }

        @JsonProperty("address4")
        public String getAddress4() {
            return address4;
        }

        @JsonProperty("address4")
        public void setAddress4(String address4) {
            this.address4 = address4;
        }

        public Address withAddress4(String address4) {
            this.address4 = address4;
            return this;
        }

        @JsonProperty("postCode")
        public String getPostCode() {
            return postCode;
        }

        @JsonProperty("postCode")
        public void setPostCode(String postCode) {
            this.postCode = postCode;
        }

        public Address withPostCode(String postCode) {
            this.postCode = postCode;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "prosecutionCounsels",
        "defenceCounsels"
    })
    public static class Attendees {

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("prosecutionCounsels")
        private List<ProsecutionCounsel> prosecutionCounsels = null;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defenceCounsels")
        private List<DefenceCounsel> defenceCounsels = null;

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("prosecutionCounsels")
        public List<ProsecutionCounsel> getProsecutionCounsels() {
            return prosecutionCounsels;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("prosecutionCounsels")
        public void setProsecutionCounsels(List<ProsecutionCounsel> prosecutionCounsels) {
            this.prosecutionCounsels = prosecutionCounsels;
        }

        public Attendees withProsecutionCounsels(List<ProsecutionCounsel> prosecutionCounsels) {
            this.prosecutionCounsels = prosecutionCounsels;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defenceCounsels")
        public List<DefenceCounsel> getDefenceCounsels() {
            return defenceCounsels;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defenceCounsels")
        public void setDefenceCounsels(List<DefenceCounsel> defenceCounsels) {
            this.defenceCounsels = defenceCounsels;
        }

        public Attendees withDefenceCounsels(List<DefenceCounsel> defenceCounsels) {
            this.defenceCounsels = defenceCounsels;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "caseId",
        "caseUrn",
        "defendants"
    })
    public static class Case {

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("caseId")
        private String caseId;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("caseUrn")
        private String caseUrn;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defendants")
        private List<Defendant> defendants = null;

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("caseId")
        public String getCaseId() {
            return caseId;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("caseId")
        public void setCaseId(String caseId) {
            this.caseId = caseId;
        }

        public Case withCaseId(String caseId) {
            this.caseId = caseId;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("caseUrn")
        public String getCaseUrn() {
            return caseUrn;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("caseUrn")
        public void setCaseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
        }

        public Case withCaseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defendants")
        public List<Defendant> getDefendants() {
            return defendants;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defendants")
        public void setDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
        }

        public Case withDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "attendeeId",
        "status",
        "defendantId",
        "title",
        "firstName",
        "lastName"
    })
    public static class DefenceCounsel {

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("attendeeId")
        private String attendeeId;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("status")
        private String status;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defendantId")
        private String defendantId;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        private String title;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        private String firstName;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        private String lastName;

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("attendeeId")
        public String getAttendeeId() {
            return attendeeId;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("attendeeId")
        public void setAttendeeId(String attendeeId) {
            this.attendeeId = attendeeId;
        }

        public DefenceCounsel withAttendeeId(String attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("status")
        public String getStatus() {
            return status;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("status")
        public void setStatus(String status) {
            this.status = status;
        }

        public DefenceCounsel withStatus(String status) {
            this.status = status;
            return this;
        }

        @JsonProperty("defendantId")
        public String getDefendantId() {
            return defendantId;
        }

        @JsonProperty("defendantId")
        public void setDefendantId(String defendantId) {
            this.defendantId = defendantId;
        }

        public DefenceCounsel withDefendantId(String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public String getTitle() {
            return title;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public void setTitle(String title) {
            this.title = title;
        }

        public DefenceCounsel withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public String getFirstName() {
            return firstName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public DefenceCounsel withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public String getLastName() {
            return lastName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public DefenceCounsel withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "defendantId",
        "personId",
        "id",
        "firstName",
        "lastName",
        "homeTelephone",
        "mobile",
        "fax",
        "email",
        "address",
        "dateOfBirth",
        "offences"
    })
    public static class Defendant {

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defendantId")
        private String defendantId;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("personId")
        private String personId;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        private String id;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        private String firstName;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        private String lastName;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("homeTelephone")
        private String homeTelephone;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("mobile")
        private String mobile;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("fax")
        private String fax;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("email")
        private String email;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("address")
        private Address address;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("dateOfBirth")
        private String dateOfBirth;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("offences")
        private List<Offence> offences = null;

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defendantId")
        public String getDefendantId() {
            return defendantId;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("defendantId")
        public void setDefendantId(String defendantId) {
            this.defendantId = defendantId;
        }

        public Defendant withDefendantId(String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("personId")
        public String getPersonId() {
            return personId;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("personId")
        public void setPersonId(String personId) {
            this.personId = personId;
        }

        public Defendant withPersonId(String personId) {
            this.personId = personId;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        public String getId() {
            return id;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        public Defendant withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public String getFirstName() {
            return firstName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public Defendant withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public String getLastName() {
            return lastName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Defendant withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("homeTelephone")
        public String getHomeTelephone() {
            return homeTelephone;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("homeTelephone")
        public void setHomeTelephone(String homeTelephone) {
            this.homeTelephone = homeTelephone;
        }

        public Defendant withHomeTelephone(String homeTelephone) {
            this.homeTelephone = homeTelephone;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("mobile")
        public String getMobile() {
            return mobile;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("mobile")
        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public Defendant withMobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("fax")
        public String getFax() {
            return fax;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("fax")
        public void setFax(String fax) {
            this.fax = fax;
        }

        public Defendant withFax(String fax) {
            this.fax = fax;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("email")
        public String getEmail() {
            return email;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("email")
        public void setEmail(String email) {
            this.email = email;
        }

        public Defendant withEmail(String email) {
            this.email = email;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("address")
        public Address getAddress() {
            return address;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("address")
        public void setAddress(Address address) {
            this.address = address;
        }

        public Defendant withAddress(Address address) {
            this.address = address;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("dateOfBirth")
        public String getDateOfBirth() {
            return dateOfBirth;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("dateOfBirth")
        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public Defendant withDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("offences")
        public List<Offence> getOffences() {
            return offences;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("offences")
        public void setOffences(List<Offence> offences) {
            this.offences = offences;
        }

        public Defendant withOffences(List<Offence> offences) {
            this.offences = offences;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "defendantId"
    })
    public static class DefendantId {

        @JsonProperty("defendantId")
        private String defendantId;

        @JsonProperty("defendantId")
        public String getDefendantId() {
            return defendantId;
        }

        @JsonProperty("defendantId")
        public void setDefendantId(String defendantId) {
            this.defendantId = defendantId;
        }

        public DefendantId withDefendantId(String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "id",
        "title",
        "firstName",
        "lastName"
    })
    public static class Judge {

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        private String id;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        private String title;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        private String firstName;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        private String lastName;

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        public String getId() {
            return id;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        public Judge withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public String getTitle() {
            return title;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public void setTitle(String title) {
            this.title = title;
        }

        public Judge withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public String getFirstName() {
            return firstName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public Judge withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public String getLastName() {
            return lastName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Judge withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "id",
        "wording",
        "count",
        "title",
        "legislation",
        "plea",
        "verdict"
    })
    public static class Offence {

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        private String id;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("wording")
        private String wording;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("count")
        private Integer count;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        private String title;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("legislation")
        private String legislation;
        @JsonProperty("plea")
        private Plea plea;
        @JsonProperty("verdict")
        private Verdict verdict;

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        public String getId() {
            return id;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        public Offence withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("wording")
        public String getWording() {
            return wording;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("wording")
        public void setWording(String wording) {
            this.wording = wording;
        }

        public Offence withWording(String wording) {
            this.wording = wording;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("count")
        public Integer getCount() {
            return count;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("count")
        public void setCount(Integer count) {
            this.count = count;
        }

        public Offence withCount(Integer count) {
            this.count = count;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public String getTitle() {
            return title;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public void setTitle(String title) {
            this.title = title;
        }

        public Offence withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("legislation")
        public String getLegislation() {
            return legislation;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("legislation")
        public void setLegislation(String legislation) {
            this.legislation = legislation;
        }

        public Offence withLegislation(String legislation) {
            this.legislation = legislation;
            return this;
        }

        @JsonProperty("plea")
        public Plea getPlea() {
            return plea;
        }

        @JsonProperty("plea")
        public void setPlea(Plea plea) {
            this.plea = plea;
        }

        public Offence withPlea(Plea plea) {
            this.plea = plea;
            return this;
        }

        @JsonProperty("verdict")
        public Verdict getVerdict() {
            return verdict;
        }

        @JsonProperty("verdict")
        public void setVerdict(Verdict verdict) {
            this.verdict = verdict;
        }

        public Offence withVerdict(Verdict verdict) {
            this.verdict = verdict;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "pleaId",
        "pleaDate",
    })
    public static class Plea {

        @JsonProperty("pleaId")
        private String pleaId;
        @JsonProperty("pleaDate")
        private String pleaDate;

        @JsonProperty("pleaId")
        public String getPleaId() {
            return pleaId;
        }

        @JsonProperty("pleaId")
        public void setPleaId(String pleaId) {
            this.pleaId = pleaId;
        }

        public Plea withPleaId(String pleaId) {
            this.pleaId = pleaId;
            return this;
        }

        @JsonProperty("pleaDate")
        public String getPleaDate() {
            return pleaDate;
        }

        @JsonProperty("pleaDate")
        public void setPleaDate(String pleaDate) {
            this.pleaDate = pleaDate;
        }

        public Plea withPleaDate(String pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "attendeeId",
        "status",
        "title",
        "firstName",
        "lastName"
    })
    public static class ProsecutionCounsel {

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("attendeeId")
        private String attendeeId;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("status")
        private String status;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        private String title;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        private String firstName;
        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        private String lastName;

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("attendeeId")
        public String getAttendeeId() {
            return attendeeId;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("attendeeId")
        public void setAttendeeId(String attendeeId) {
            this.attendeeId = attendeeId;
        }

        public ProsecutionCounsel withAttendeeId(String attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("status")
        public String getStatus() {
            return status;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("status")
        public void setStatus(String status) {
            this.status = status;
        }

        public ProsecutionCounsel withStatus(String status) {
            this.status = status;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public String getTitle() {
            return title;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("title")
        public void setTitle(String title) {
            this.title = title;
        }

        public ProsecutionCounsel withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public String getFirstName() {
            return firstName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("firstName")
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public ProsecutionCounsel withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public String getLastName() {
            return lastName;
        }

        /**
         * 
         * (Required)
         * 
         */
        @JsonProperty("lastName")
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public ProsecutionCounsel withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "id",
        "category",
        "code",
        "description"
    })
    public static class Value {

        @JsonProperty("id")
        private String id;
        @JsonProperty("category")
        private String category;
        @JsonProperty("code")
        private String code;
        @JsonProperty("description")
        private String description;

        @JsonProperty("id")
        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        public Value withId(String id) {
            this.id = id;
            return this;
        }

        @JsonProperty("category")
        public String getCategory() {
            return category;
        }

        @JsonProperty("category")
        public void setCategory(String category) {
            this.category = category;
        }

        public Value withCategory(String category) {
            this.category = category;
            return this;
        }

        @JsonProperty("code")
        public String getCode() {
            return code;
        }

        @JsonProperty("code")
        public void setCode(String code) {
            this.code = code;
        }

        public Value withCode(String code) {
            this.code = code;
            return this;
        }

        @JsonProperty("description")
        public String getDescription() {
            return description;
        }

        @JsonProperty("description")
        public void setDescription(String description) {
            this.description = description;
        }

        public Value withDescription(String description) {
            this.description = description;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "verdictId",
        "hearingId",
        "value",
        "verdictDate",
        "numberOfSplitJurors",
        "numberOfJurors",
        "unanimous"
    })
    public static class  Verdict {

        @JsonProperty("verdictId")
        private String verdictId;
        @JsonProperty("hearingId")
        private String hearingId;
        @JsonProperty("value")
        private Value value;
        @JsonProperty("verdictDate")
        private String verdictDate;
        @JsonProperty("numberOfSplitJurors")
        private Integer numberOfSplitJurors;
        @JsonProperty("numberOfJurors")
        private Integer numberOfJurors;
        @JsonProperty("unanimous")
        private Boolean unanimous;

        @JsonProperty("verdictId")
        public String getVerdictId() {
            return verdictId;
        }

        @JsonProperty("verdictId")
        public void setVerdictId(String verdictId) {
            this.verdictId = verdictId;
        }

        public Verdict withVerdictId(String verdictId) {
            this.verdictId = verdictId;
            return this;
        }

        @JsonProperty("hearingId")
        public String getHearingId() {
            return hearingId;
        }

        @JsonProperty("hearingId")
        public void setHearingId(String hearingId) {
            this.hearingId = hearingId;
        }

        public Verdict withHearingId(String hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        @JsonProperty("value")
        public Value getValue() {
            return value;
        }

        @JsonProperty("value")
        public void setValue(Value value) {
            this.value = value;
        }

        public Verdict withValue(Value value) {
            this.value = value;
            return this;
        }

        @JsonProperty("verdictDate")
        public String getVerdictDate() {
            return verdictDate;
        }

        @JsonProperty("verdictDate")
        public void setVerdictDate(String verdictDate) {
            this.verdictDate = verdictDate;
        }

        public Verdict withVerdictDate(String verdictDate) {
            this.verdictDate = verdictDate;
            return this;
        }

        @JsonProperty("numberOfSplitJurors")
        public Integer getNumberOfSplitJurors() {
            return numberOfSplitJurors;
        }

        @JsonProperty("numberOfSplitJurors")
        public void setNumberOfSplitJurors(Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
        }

        public Verdict withNumberOfSplitJurors(Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
            return this;
        }

        @JsonProperty("numberOfJurors")
        public Integer getNumberOfJurors() {
            return numberOfJurors;
        }

        @JsonProperty("numberOfJurors")
        public void setNumberOfJurors(Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
        }

        public Verdict withNumberOfJurors(Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
            return this;
        }

        @JsonProperty("unanimous")
        public Boolean getUnanimous() {
            return unanimous;
        }

        @JsonProperty("unanimous")
        public void setUnanimous(Boolean unanimous) {
            this.unanimous = unanimous;
        }

        public Verdict withUnanimous(Boolean unanimous) {
            this.unanimous = unanimous;
            return this;
        }

    }

}