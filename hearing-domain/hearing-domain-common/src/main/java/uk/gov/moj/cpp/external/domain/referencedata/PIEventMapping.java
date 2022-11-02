package uk.gov.moj.cpp.external.domain.referencedata;

import java.io.Serializable;
import java.util.UUID;

public class PIEventMapping implements Serializable {

    private UUID cpHearingEventId;
    private String piHearingEventCode;
    private String piHearingEventDescription;

    public PIEventMapping(final UUID cpHearingEventId, final String piHearingEventCode, final String piHearingEventDescription) {
        this.cpHearingEventId = cpHearingEventId;
        this.piHearingEventCode = piHearingEventCode;
        this.piHearingEventDescription = piHearingEventDescription;
    }

    public UUID getCpHearingEventId() {
        return cpHearingEventId;
    }

    public void setCpHearingEventId(final UUID cpHearingEventId) {
        this.cpHearingEventId = cpHearingEventId;
    }

    public String getPiHearingEventCode() {
        return piHearingEventCode;
    }

    public void setPiHearingEventCode(final String piHearingEventCode) {
        this.piHearingEventCode = piHearingEventCode;
    }

    public String getPiHearingEventDescription() {
        return piHearingEventDescription;
    }

    public void setPiHearingEventDescription(final String piHearingEventDescription) {
        this.piHearingEventDescription = piHearingEventDescription;
    }

}
