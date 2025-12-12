package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.courtcentre;

import java.util.UUID;

public class CourtCentreCode {

    private UUID id;
    private String oucode;
    private String crestCodeId;
    private String crestCourtSiteId;
    private String crestCourtSiteName;
    private String validFrom;

    public CourtCentreCode(final UUID id, final String oucode, final String crestCodeId, final String crestCourtSiteId, final String crestCourtSiteName, final String validFrom) {
        this.id = id;
        this.oucode = oucode;
        this.crestCodeId = crestCodeId;
        this.crestCourtSiteId = crestCourtSiteId;
        this.crestCourtSiteName = crestCourtSiteName;
        this.validFrom = validFrom;
    }

    public UUID getId() {
        return id;
    }

    public String getOucode() {
        return oucode;
    }

    public String getCrestCodeId() {
        return crestCodeId;
    }

    public String getCrestCourtSiteId() {
        return crestCourtSiteId;
    }

    public String getCrestCourtSiteName() {
        return crestCourtSiteName;
    }

    public String getValidFrom() {
        return validFrom;
    }
}
