package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings({"squid:S2384"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class NowDefinition {

    private UUID id;

    private String name;

    private String welshName;

    private String templateName;

    private String subTemplateName;

    private String bilingualTemplateName;

    private Integer rank;

    private String jurisdiction;

    private String text;

    private String welshText;

    private Boolean remotePrintingRequired;

    private ZonedDateTime version;

    private Date startDate;

    private Date endDate;

    private List<NowResultDefinitionRequirement> resultDefinitions;

    private Integer urgentTimeLimitInMinutes;

    private LocalDate referenceDate;

    public static NowDefinition now() {
        return new NowDefinition();
    }

    public UUID getId() {
        return this.id;
    }

    public NowDefinition setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public NowDefinition setName(final String name) {
        this.name = name;
        return this;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public NowDefinition setTemplateName(final String templateName) {
        this.templateName = templateName;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public NowDefinition setRank(final Integer rank) {
        this.rank = rank;
        return this;
    }

    public String getJurisdiction() {
        return this.jurisdiction;
    }

    public NowDefinition setJurisdiction(final String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public ZonedDateTime getVersion() {
        return this.version;
    }

    public NowDefinition setVersion(final ZonedDateTime version) {
        this.version = version;
        return this;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public NowDefinition setStartDate(final Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public NowDefinition setEndDate(final Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public List<NowResultDefinitionRequirement> getResultDefinitions() {
        return this.resultDefinitions;
    }

    public NowDefinition setResultDefinitions(final List<NowResultDefinitionRequirement> resultDefinitions) {
        this.resultDefinitions = resultDefinitions;
        return this;
    }

    public Number getUrgentTimeLimitInMinutes() {
        return this.urgentTimeLimitInMinutes;
    }

    public NowDefinition setUrgentTimeLimitInMinutes(final Integer urgentTimeLimitInMinutes) {
        this.urgentTimeLimitInMinutes = urgentTimeLimitInMinutes;
        return this;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public NowDefinition setReferenceDate(final LocalDate referenceDate) {
        this.referenceDate = referenceDate;
        return this;
    }

    public String getText() {
        return text;
    }

    public NowDefinition setText(String text) {
        this.text = text;
        return this;
    }

    public String getWelshText() {
        return welshText;
    }

    public NowDefinition setWelshText(String welshText) {
        this.welshText = welshText;
        return this;
    }

    public String getWelshName() {
        return welshName;
    }

    public NowDefinition setWelshName(final String welshName) {
        this.welshName = welshName;
        return this;
    }

    public String getBilingualTemplateName() {
        return bilingualTemplateName;
    }

    public NowDefinition setBilingualTemplateName(final String bilingualTemplateName) {
        this.bilingualTemplateName = bilingualTemplateName;
        return this;
    }

    public Boolean getRemotePrintingRequired() {
        return remotePrintingRequired;
    }

    public NowDefinition setRemotePrintingRequired(final Boolean remotePrintingRequired) {
        this.remotePrintingRequired = remotePrintingRequired;
        return this;
    }

    public String getSubTemplateName() {
        return subTemplateName;
    }

    public void setSubTemplateName(String subTemplateName) {
        this.subTemplateName = subTemplateName;
    }
}
