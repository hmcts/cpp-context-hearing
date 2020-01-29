package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.UUID;

public class CourtRoomMappings {

    private UUID id;
    private String oucode;
    private Integer courtRoomId;
    private String crestCourtId;
    private String crestCourtSiteId;
    private String crestCourtSiteCode;
    private String crestCourtRoomName;

    public CourtRoomMappings(final UUID id, final String oucode, final Integer courtRoomId, final String crestCourtId, final String crestCourtSiteId, final String crestCourtSiteCode, final String crestCourtRoomName) {
        this.id = id;
        this.oucode = oucode;
        this.courtRoomId = courtRoomId;
        this.crestCourtId = crestCourtId;
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

    public Integer getCourtRoomId() {
        return courtRoomId;
    }

    public String getCrestCourtId() {
        return crestCourtId;
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
