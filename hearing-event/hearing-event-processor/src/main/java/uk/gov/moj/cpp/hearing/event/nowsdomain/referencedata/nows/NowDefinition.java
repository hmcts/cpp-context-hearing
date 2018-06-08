package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.util.List;
import java.util.UUID;

public class NowDefinition {

    private UUID id;

    private String version;

    private String name;

    private String templateName;

    private String jurisdiction;

    private Integer rank;

    private String text;

    private String welshText;

    private Integer urgentTimeLimitInMinutes;

    private List<ResultDefinitions> resultDefinitions;

    public UUID getId() {
        return this.id;
    }

    public NowDefinition setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public NowDefinition setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public NowDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public NowDefinition setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getJurisdiction() {
        return this.jurisdiction;
    }

    public NowDefinition setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public NowDefinition setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public String getText() {
        return this.text;
    }

    public NowDefinition setText(String text) {
        this.text = text;
        return this;
    }

    public String getWelshText() {
        return this.welshText;
    }

    public NowDefinition setWelshText(String welshText) {
        this.welshText = welshText;
        return this;
    }

    public Integer getUrgentTimeLimitInMinutes() {
        return this.urgentTimeLimitInMinutes;
    }

    public NowDefinition setUrgentTimeLimitInMinutes(Integer urgentTimeLimitInMinutes) {
        this.urgentTimeLimitInMinutes = urgentTimeLimitInMinutes;
        return this;
    }

    public List<ResultDefinitions> getResultDefinitions() {
        return this.resultDefinitions;
    }

    public NowDefinition setResultDefinitions(List<ResultDefinitions> resultDefinitions) {
        this.resultDefinitions = resultDefinitions;
        return this;
    }

    public static NowDefinition now() {
        return new NowDefinition();
    }
}
