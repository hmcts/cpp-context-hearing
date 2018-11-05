package uk.gov.moj.cpp.hearing.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Plea implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID originHearingId;
    private UUID offenceId;
    private LocalDate pleaDate;
    private String value;

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }

    public Plea setOriginHearingId(UUID originHearingId) {
        this.originHearingId = originHearingId;
        return this;
    }

    public Plea setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public Plea setPleaDate(LocalDate pleaDate) {
        this.pleaDate = pleaDate;
        return this;
    }

    public Plea setValue(String value) {
        this.value = value;
        return this;
    }

    public static Plea plea() {
        return new Plea();
    }
}
