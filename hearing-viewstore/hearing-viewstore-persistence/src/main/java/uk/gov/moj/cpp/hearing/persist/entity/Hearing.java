package uk.gov.moj.cpp.hearing.persist.entity;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import uk.gov.moj.cpp.hearing.domain.HearingStatusEnum;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
/**
 * Hearing entity
 * @author hshaik
 * 
 */
@Entity
@Table(name = "Hearing")
public class Hearing {  

    @Id
    @Column(name = "hearingid", unique = true, nullable = false)
    private UUID hearingId;

    @Column
    private UUID caseId;

    @Enumerated(EnumType.STRING)
    private HearingTypeEnum hearingType;

    @Column(name = "startdate")
    private LocalDate startDate;

    @Column(nullable = false, name = "duration")
    private Integer duration;

    @Column(name = "courtCentreName")
    private String courtCentreName;

    @Enumerated(EnumType.STRING)
    private HearingStatusEnum status;
    
    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }

    public HearingTypeEnum getHearingType() {
        return hearingType;
    }

    public void setHearingType(HearingTypeEnum hearingType) {
        this.hearingType = hearingType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public UUID geHearingtId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public HearingStatusEnum getStatus() {
        return status;
    }

    public void setStatus(HearingStatusEnum status) {
        this.status = status;
    }

}
