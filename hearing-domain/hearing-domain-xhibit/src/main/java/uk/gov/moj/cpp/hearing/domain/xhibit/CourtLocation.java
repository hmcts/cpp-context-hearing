package uk.gov.moj.cpp.hearing.domain.xhibit;

@SuppressWarnings({"pmd:BeanMembersShouldSerialize"})
public class CourtLocation {

    private String ouCode;
    private String crestCourtId;        // e.g. 433 - CREST identifier for the crown court (aka parent court) of the court site
    private String crestCourtSiteId;    // e.g. 433 - CREST identifier for a court site (aka court house, court centre)
    private String courtName;           // e.g. LIVERPOOL
    private String courtShortName;      // e.g. LIVER
    private String courtSiteName;       // e.g. LIVERPOOL
    private String courtSiteCode;       // e.g. A
    private String courtType;           // e.g CROWN_COURT

    @SuppressWarnings({"squid:S00107"})
    public CourtLocation(
            final String ouCode,
            final String crestCourtId,
            final String crestCourtSiteId,
            final String courtName,
            final String courtShortName,
            final String courtSiteName,
            final String courtSiteCode,
            final String courtType) {
        this.ouCode = ouCode;
        this.crestCourtId = crestCourtId;
        this.crestCourtSiteId = crestCourtSiteId;
        this.courtName = courtName;
        this.courtShortName = courtShortName;
        this.courtSiteName = courtSiteName;
        this.courtSiteCode = courtSiteCode;
        this.courtType = courtType;
    }

    public String getOuCode() {
        return ouCode;
    }

    public String getCrestCourtId() {
        return crestCourtId;
    }

    public String getCrestCourtSiteId() {
        return crestCourtSiteId;
    }

    public String getCourtName() {
        return courtName;
    }

    public String getCourtSiteName() {
        return courtSiteName;
    }

    public String getCourtShortName() {
        return courtShortName;
    }

    public String getCourtSiteCode() {
        return courtSiteCode;
    }

    public String getCourtType() {
        return courtType;
    }


}
