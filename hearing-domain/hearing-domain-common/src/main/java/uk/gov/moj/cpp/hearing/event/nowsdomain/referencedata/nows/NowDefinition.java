package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.time.LocalDate;
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

    private String text;

    private String welshText;

    private LocalDate referenceDate;

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

    public LocalDate getReferenceDate() {
        return referenceDate;
    }
    public NowDefinition setId(final UUID id) {
        this.id = id;
        return this;
    }

    public NowDefinition setName(final String name) {
        this.name = name;
        return this;
    }

    public NowDefinition setTemplateName(final String templateName) {
        this.templateName = templateName;
        return this;
    }

    public NowDefinition setRank(final Integer rank) {
        this.rank = rank;
        return this;
    }

    public NowDefinition setJurisdiction(final String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public NowDefinition setVersion(final ZonedDateTime version) {
        this.version = version;
        return this;
    }

    public NowDefinition setStartDate(final Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public NowDefinition setEndDate(final Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public NowDefinition setResultDefinitions(final List<ResultDefinitions> resultDefinitions) {
        this.resultDefinitions = resultDefinitions;
        return this;
    }

    public NowDefinition setUrgentTimeLimitInMinutes(final Integer urgentTimeLimitInMinutes) {
        this.urgentTimeLimitInMinutes = urgentTimeLimitInMinutes;
        return this;
    }

    public NowDefinition setText(String text) {
        this.text = text;
        return this;
    }

    public NowDefinition setWelshText(String welshText) {
        this.welshText = welshText;
        return this;
    }

    public String getText() {
        return text;
    }

    public String getWelshText() {
        return welshText;
    }

    public NowDefinition setReferenceDate(final LocalDate referenceDate) {
        this.referenceDate = referenceDate;
        return this;
    }

    public static NowDefinition now() {
        return new NowDefinition();
    }

}
