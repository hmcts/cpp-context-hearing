package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NowDefinition {

    private UUID id;

    private String name;

    private String templateName;

    private Integer rank;

    private String jurisdiction;

    private ZonedDateTime version;

    private Date startDate;

    private Date endDate;

    private List<ResultDefinitions> resultDefinitions;

    private Integer urgentTimeLimitInMinutes;

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public Integer getRank() {
        return this.rank;
    }

    public String getJurisdiction() {
        return this.jurisdiction;
    }

    public ZonedDateTime getVersion() {
        return this.version;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public List<ResultDefinitions> getResultDefinitions() {
        return this.resultDefinitions;
    }

    public Number getUrgentTimeLimitInMinutes() {
        return this.urgentTimeLimitInMinutes;
    }

    public NowDefinition setId(UUID id) {
        this.id = id;
        return this;
    }

    public NowDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public NowDefinition setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public NowDefinition setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public NowDefinition setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public NowDefinition setVersion(ZonedDateTime version) {
        this.version = version;
        return this;
    }

    public NowDefinition setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public NowDefinition setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public NowDefinition setResultDefinitions(List<ResultDefinitions> resultDefinitions) {
        this.resultDefinitions = resultDefinitions;
        return this;
    }

    public NowDefinition setUrgentTimeLimitInMinutes(Integer urgentTimeLimitInMinutes) {
        this.urgentTimeLimitInMinutes = urgentTimeLimitInMinutes;
        return this;
    }

    public static NowDefinition now() {
        return new NowDefinition();
    }
}
