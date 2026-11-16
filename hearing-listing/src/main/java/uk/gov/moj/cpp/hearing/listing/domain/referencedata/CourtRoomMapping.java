package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.util.UUID;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class CourtRoomMapping  {

    private UUID id;
    private UUID courtRoomUUID;
    private String crestCourtSiteName;
    private String oucode;
    private Integer courtRoomId;
    private String crestCourtId;
    private String crestCourtSiteId;
    private String crestCourtSiteCode;
    private String crestCourtRoomName;
    private UUID crestCourtSiteUUID;

    public CourtRoomMapping(UUID id, UUID courtRoomUUID, String crestCourtSiteName, String oucode, Integer courtRoomId, String crestCourtId, String crestCourtSiteId, String crestCourtSiteCode, String crestCourtRoomName, UUID crestCourtSiteUUID) {
        this.id = id;
        this.courtRoomUUID = courtRoomUUID;
        this.crestCourtSiteName = crestCourtSiteName;
        this.oucode = oucode;
        this.courtRoomId = courtRoomId;
        this.crestCourtId = crestCourtId;
        this.crestCourtSiteId = crestCourtSiteId;
        this.crestCourtSiteCode = crestCourtSiteCode;
        this.crestCourtRoomName = crestCourtRoomName;
        this.crestCourtSiteUUID = crestCourtSiteUUID;
    }

    public CourtRoomMapping(String crestCourtRoomName) {
        this.crestCourtRoomName = crestCourtRoomName;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCourtRoomUUID() {
        return courtRoomUUID;
    }

    public String getCrestCourtSiteName() {
        return crestCourtSiteName;
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

    public UUID getCrestCourtSiteUUID() {
        return crestCourtSiteUUID;
    }

    public static class Builder {
        private UUID id;
        private UUID courtRoomUUID;
        private String crestCourtSiteName;
        private String oucode;
        private Integer courtRoomId;
        private String crestCourtId;
        private String crestCourtSiteId;
        private String crestCourtSiteCode;
        private String crestCourtRoomName;
        private UUID crestCourtSiteUUID;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withCrestCourtSiteCode(final String crestCourtSiteCode) {
            this.crestCourtSiteCode = crestCourtSiteCode;
            return this;
        }

        public Builder withCrestCourtRoomName(final String crestCourtRoomName) {
            this.crestCourtRoomName = crestCourtRoomName;
            return this;
        }

        public CourtRoomMapping build() {
            return new CourtRoomMapping(id, courtRoomUUID, crestCourtSiteName, oucode, courtRoomId, crestCourtId, crestCourtSiteId, crestCourtSiteCode, crestCourtRoomName, crestCourtSiteUUID);
        }
    }


}
