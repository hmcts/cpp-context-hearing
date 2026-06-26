package uk.gov.moj.cpp.hearing.persist.entity.sessiontime;


import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ha_session_time")
public class SessionTime {

    @Id
    @Column(name = "court_session_id", nullable = false)
    private UUID courtSessionId;

    @Column(name = "court_session_date", nullable = false)
    private LocalDate courtSessionDate;

    @Column(name = "court_house_id", nullable = false)
    private UUID courtHouseId;

    @Column(name = "court_room_id", nullable = false)
    private UUID courtRoomId;

    @Column(name = "am_court_session", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode amCourtSession;

    @Column(name = "pm_court_session", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode pmCourtSession;

    public SessionTime() {
        //For JPA
    }

    public UUID getCourtSessionId() {
        return courtSessionId;
    }

    public void setCourtSessionId(UUID courtSessionId) {
        this.courtSessionId = courtSessionId;
    }

    public LocalDate getCourtSessionDate() {
        return courtSessionDate;
    }

    public void setCourtSessionDate(LocalDate courtSessionDate) {
        this.courtSessionDate = courtSessionDate;
    }

    public UUID getCourtHouseId() {
        return courtHouseId;
    }

    public void setCourtHouseId(UUID courtHouseId) {
        this.courtHouseId = courtHouseId;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public void setCourtRoomId(UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
    }

    public JsonNode getAmCourtSession() {
        return amCourtSession;
    }

    public void setAmCourtSession(JsonNode amCourtSession) {
        this.amCourtSession = amCourtSession;
    }

    public JsonNode getPmCourtSession() {
        return pmCourtSession;
    }

    public void setPmCourtSession(JsonNode pmCourtSession) {
        this.pmCourtSession = pmCourtSession;
    }
}
