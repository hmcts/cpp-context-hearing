
package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.List;

public class HearingListResponse {

    private List<Hearing> hearings = null;

    public List<Hearing> getHearings() {
        return hearings;
    }

    public void setHearings(List<Hearing> hearings) {
        this.hearings = hearings;
    }

    public HearingListResponse withHearings(List<Hearing> hearings) {
        this.hearings = hearings;
        return this;
    }

    public static class Defendant {

        private String firstName;
        private String lastName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public Defendant withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Defendant withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

    }

    public static class Hearing {

        private String hearingId;
        private String hearingType;
        private List<String> caseUrn = null;
        private List<Defendant> defendants = null;

        public String getHearingId() {
            return hearingId;
        }

        public void setHearingId(String hearingId) {
            this.hearingId = hearingId;
        }

        public Hearing withHearingId(String hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public String getHearingType() {
            return hearingType;
        }

        public void setHearingType(String hearingType) {
            this.hearingType = hearingType;
        }

        public Hearing withHearingType(String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public List<String> getCaseUrn() {
            return caseUrn;
        }

        public void setCaseUrn(List<String> caseUrn) {
            this.caseUrn = caseUrn;
        }

        public Hearing withCaseUrn(List<String> caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public List<Defendant> getDefendants() {
            return defendants;
        }

        public void setDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
        }

        public Hearing withDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

    }

}