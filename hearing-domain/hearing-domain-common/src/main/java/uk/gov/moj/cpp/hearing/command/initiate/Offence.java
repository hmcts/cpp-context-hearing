package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Offence implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID caseId;
    private String offenceCode;
    private String wording;
    private String section;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer orderIndex;
    private Integer count;
    private LocalDate convictionDate;
    private String title;
    private String legislation;

    public Offence(){}

    @JsonCreator
    public Offence(@JsonProperty("id") final UUID id,
                   @JsonProperty("caseId") final UUID caseId,
                   @JsonProperty("offenceCode") final String offenceCode,
                   @JsonProperty("wording") final String wording,
                   @JsonProperty("section") final String section,
                   @JsonProperty("startDate") final LocalDate startDate,
                   @JsonProperty("endDate") final LocalDate endDate,
                   @JsonProperty("orderIndex") final Integer orderIndex,
                   @JsonProperty("count") final Integer count,
                   @JsonProperty("convictionDate") final LocalDate convictionDate,
                   @JsonProperty("title") final String title,
                   @JsonProperty("legislation") final String legislation
    ) {
        this.id = id;
        this.caseId = caseId;
        this.offenceCode = offenceCode;
        this.wording = wording;
        this.section = section;
        this.startDate = startDate;
        this.endDate = endDate;
        this.orderIndex = orderIndex;
        this.count = count;
        this.convictionDate = convictionDate;
        this.title = title;
        this.legislation = legislation;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public Offence setOffenceCode(final String offenceCode) {
        this.offenceCode = offenceCode;
        return this;
    }

    public String getWording() {
        return wording;
    }

    public Offence setWording(final String wording) {
        this.wording = wording;
        return this;
    }

    public String getSection() {
        return section;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getStartDate() {
        return startDate;
    }

    public Offence setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getEndDate() {
        return endDate;
    }

    public Offence setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public Integer getCount() {
        return count;
    }

    public Offence setCount(final Integer count) {
        this.count = count;
        return this;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public Offence setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getLegislation() {
        return legislation;
    }

    public Offence setId(UUID id) {
        this.id = id;
        return this;
    }

    public Offence setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public Offence setSection(String section) {
        this.section = section;
        return this;
    }

    public Offence setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
        return this;
    }

    public Offence setTitle(String title) {
        this.title = title;
        return this;
    }

    public Offence setLegislation(String legislation) {
        this.legislation = legislation;
        return this;
    }

    public static Offence offence(){
        return new Offence();
    }
}
