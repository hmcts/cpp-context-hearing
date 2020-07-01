package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultDefinition {

    public static final String YES = "Y";

    private UUID id;

    private String label;

    private String shortCode;

    private String level;

    private Integer rank;

    private List<WordGroups> wordGroups;

    private List<String> userGroups = new ArrayList<>();

    private String version;

    private List<Prompt> prompts = new ArrayList<>();

    private Date startDate;

    private Date endDate;

    private String welshLabel;

    private Boolean isAvailableForCourtExtract;

    private String financial;

    private String category;

    private String cjsCode;

    private String adjournment;

    private String convicted;

    private String qualifier;

    private String postHearingCustodyStatus;

    private String resultDefinitionGroup;

    private Boolean terminatesOffenceProceedings;

    private Boolean unscheduled;

    private Boolean lifeDuration;

    private Boolean publishedAsAPrompt;

    private Boolean excludedFromResults;

    private Boolean alwaysPublished;

    private Boolean urgent;

    private Boolean d20;

    private List<ResultDefinitionRuleType> resultDefinitionRules = new ArrayList<>();

    public static ResultDefinition resultDefinition() {
        return new ResultDefinition();
    }

    public String getResultDefinitionGroup() {
        return resultDefinitionGroup;
    }

    public ResultDefinition setResultDefinitionGroup(final String resultDefinitionGroup) {
        this.resultDefinitionGroup = resultDefinitionGroup;
        return this;
    }

    public String getPostHearingCustodyStatus() {
        return postHearingCustodyStatus;
    }

    public ResultDefinition setPostHearingCustodyStatus(final String postHearingCustodyStatus) {
        this.postHearingCustodyStatus = postHearingCustodyStatus;
        return this;
    }

    public UUID getId() {
        return this.id;
    }

    public ResultDefinition setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public ResultDefinition setLabel(final String label) {
        this.label = label;
        return this;
    }

    public String getShortCode() {
        return this.shortCode;
    }

    public ResultDefinition setShortCode(final String shortCode) {
        this.shortCode = shortCode;
        return this;
    }

    public String getLevel() {
        return this.level;
    }

    public ResultDefinition setLevel(final String level) {
        this.level = level;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public ResultDefinition setRank(final Integer rank) {
        this.rank = rank;
        return this;
    }

    public List<WordGroups> getWordGroups() {
        return this.wordGroups;
    }

    public ResultDefinition setWordGroups(final List<WordGroups> wordGroups) {
        this.wordGroups = wordGroups;
        return this;
    }

    public List<String> getUserGroups() {
        return this.userGroups;
    }

    public ResultDefinition setUserGroups(final List<String> userGroups) {
        this.userGroups = userGroups;
        return this;
    }

    public List<Prompt> getPrompts() {
        return this.prompts;
    }

    public ResultDefinition setPrompts(final List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public ResultDefinition setStartDate(final Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public ResultDefinition setEndDate(final Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ResultDefinition setVersion(final String version) {
        this.version = version;
        return this;
    }

    public String getFinancial() {
        return financial;
    }

    public ResultDefinition setFinancial(final String financial) {
        this.financial = financial;
        return this;
    }

    public Boolean getIsAvailableForCourtExtract() {
        return isAvailableForCourtExtract;
    }

    public ResultDefinition setIsAvailableForCourtExtract(final Boolean isAvailableForCourtExtract) {
        this.isAvailableForCourtExtract = isAvailableForCourtExtract;
        return this;
    }

    public String getWelshLabel() {
        return welshLabel;
    }

    public ResultDefinition setWelshLabel(final String welshLabel) {
        this.welshLabel = welshLabel;
        return this;
    }

    public String getCategory() {
        return this.category;
    }

    public ResultDefinition setCategory(final String category) {
        this.category = category;
        return this;
    }

    public String getCjsCode() {
        return this.cjsCode;
    }

    public ResultDefinition setCjsCode(final String cjsCode) {
        this.cjsCode = cjsCode;
        return this;
    }

    public boolean isAdjournment() {
        return adjournment != null && adjournment.equalsIgnoreCase(YES);
    }

    public ResultDefinition setAdjournment(final String adjournment) {
        this.adjournment = adjournment;
        return this;
    }

    public boolean isConvicted() {
        return convicted != null && convicted.equalsIgnoreCase(YES);
    }

    public ResultDefinition setConvicted(final String convicted) {
        this.convicted = convicted;
        return this;
    }

    public String getQualifier() {
        return this.qualifier;
    }

    public ResultDefinition setQualifier(final String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public Boolean getLifeDuration() {
        return this.lifeDuration;
    }

    public ResultDefinition setLifeDuration(final Boolean lifeDuration) {
        this.lifeDuration = lifeDuration;
        return this;
    }

    public Boolean getPublishedAsAPrompt() {
        return this.publishedAsAPrompt;
    }

    public ResultDefinition setPublishedAsAPrompt(final Boolean publishedAsAPrompt) {
        this.publishedAsAPrompt = publishedAsAPrompt;
        return this;
    }

    public Boolean getExcludedFromResults() {
        return this.excludedFromResults;
    }

    public ResultDefinition setExcludedFromResults(final Boolean excludedFromResults) {
        this.excludedFromResults = excludedFromResults;
        return this;
    }

    public Boolean getAlwaysPublished() {
        return this.alwaysPublished;
    }

    public ResultDefinition setAlwaysPublished(final Boolean alwaysPublished) {
        this.alwaysPublished = alwaysPublished;
        return this;
    }

    public Boolean getTerminatesOffenceProceedings() {
        return this.terminatesOffenceProceedings;
    }

    public ResultDefinition setTerminatesOffenceProceedings(final Boolean terminatesOffenceProceedings) {
        this.terminatesOffenceProceedings = terminatesOffenceProceedings;
        return this;
    }

    public Boolean getUrgent() {
        return this.urgent;
    }

    public ResultDefinition setUrgent(final Boolean urgent) {
        this.urgent = urgent;
        return this;
    }

    public Boolean getD20() {
        return this.d20;
    }

    public ResultDefinition setD20(final Boolean d20) {
        this.d20 = d20;
        return this;
    }

    public List<ResultDefinitionRuleType> getResultDefinitionRules() {
        return resultDefinitionRules;
    }

    public ResultDefinition setResultDefinitionRules(final List<ResultDefinitionRuleType> resultDefinitionRules) {
        this.resultDefinitionRules = resultDefinitionRules;
        return this;
    }

    public Boolean getUnscheduled() {
        return unscheduled;
    }

    public ResultDefinition setUnscheduled(final Boolean unscheduled) {
        this.unscheduled = unscheduled;
        return this;
    }
}
