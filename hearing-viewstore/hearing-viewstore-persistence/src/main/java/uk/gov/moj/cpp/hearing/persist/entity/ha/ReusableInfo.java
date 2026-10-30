package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ha_reusable_info")
public class ReusableInfo {

    @Id
    @Column(name = "master_defendant_id", nullable = false)
    private UUID masterDefendantId;

    @Column(name = "payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;

    @Column(name = "last_updated_dt")
    private ZonedDateTime localUpdatedDate;

    public ReusableInfo(){

    }

    public ReusableInfo(final UUID  masterDefendantId, final JsonNode payload, final ZonedDateTime localUpdatedDate) {
        this.masterDefendantId = masterDefendantId;
        this.payload = payload;
        this.localUpdatedDate = localUpdatedDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return masterDefendantId;
    }

    public void setId(final UUID masterDefendantId) {
        this.masterDefendantId = masterDefendantId;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(final JsonNode payload) {
        this.payload = payload;
    }

    public ZonedDateTime getLocalUpdatedDate() {
        return localUpdatedDate;
    }

    public void setLocalUpdatedDate(final ZonedDateTime localUpdatedDate) {
        this.localUpdatedDate = localUpdatedDate;
    }

    public static final class Builder {
        private UUID masterDefendantId;
        private JsonNode payload;
        private ZonedDateTime localUpdatedDate;

        public Builder withId(UUID masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
            return this;
        }

        public Builder withPayload(JsonNode payload) {
            this.payload = payload;
            return this;
        }

        public Builder withLocalUpdatedDate(ZonedDateTime localUpdatedDate) {
            this.localUpdatedDate = localUpdatedDate;
            return this;
        }

        public ReusableInfo build() {
            return new ReusableInfo(masterDefendantId, payload, localUpdatedDate);
        }

    }

}
