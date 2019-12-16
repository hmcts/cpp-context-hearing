package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.UUID;

public class CourtRoomMappings {

    private UUID id;
    private String oucode;
    private String courtRoomId;
    private String crestCodeId;
    private String crestCourtSiteId;
    private String crestCourtSiteCode;
    private String crestCourtRoomName;


    public CourtRoomMappings(final UUID id, final String oucode, final String courtRoomId, final String crestCodeId, final String crestCourtSiteId, final String crestCourtSiteCode, final String crestCourtRoomName) {
        this.id = id;
        this.oucode = oucode;
        this.courtRoomId = courtRoomId;
        this.crestCodeId = crestCodeId;
        this.crestCourtSiteId = crestCourtSiteId;
        this.crestCourtSiteCode = crestCourtSiteCode;
        this.crestCourtRoomName = crestCourtRoomName;
    }

    public UUID getId() {
        return id;
    }

    public String getOucode() {
        return oucode;
    }

    public String getCourtRoomId() {
        return courtRoomId;
    }

    public String getCrestCodeId() {
        return crestCodeId;
    }

    public String getCrestCourtSiteId() {
        return crestCourtSiteId;
    }

    public String getCrestCourtSiteCode() {
        return crestCourtSiteCode;
    }

    public String getCrestCourtRoomName() {
        return crestCourtRoomName;
    }
}
