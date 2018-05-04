package uk.gov.moj.cpp.hearing.event.nows.domain;

import java.io.Serializable;

public class NowsOrderCourtCentre implements Serializable {

    private final static long serialVersionUID = 3366751186328484548L;
    private String courtCentreName;
    private String courtRoomName;

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public void setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        private String courtCentreName;
        private String courtRoomName;

        private Builder() {
        }



        public Builder withCourtCentreName(String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withCourtRoomName(String courtRoomName) {
            this.courtRoomName = courtRoomName;
            return this;
        }

        public NowsOrderCourtCentre build() {
            NowsOrderCourtCentre courtCentreInOrder = new NowsOrderCourtCentre();
            courtCentreInOrder.setCourtCentreName(courtCentreName);
            courtCentreInOrder.setCourtRoomName(courtRoomName);
            return courtCentreInOrder;
        }
    }
}
