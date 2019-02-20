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

    public static ResultDefinition resultDefinition() {
        return new ResultDefinition();
    }

    public UUID getId() {
        return this.id;
    }

    public ResultDefinition setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public ResultDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getShortCode() {
        return this.shortCode;
    }

    public ResultDefinition setShortCode(String shortCode) {
        this.shortCode = shortCode;
        return this;
    }

    public String getLevel() {
        return this.level;
    }

    public ResultDefinition setLevel(String level) {
        this.level = level;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public ResultDefinition setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public List<WordGroups> getWordGroups() {
        return this.wordGroups;
    }

    public ResultDefinition setWordGroups(List<WordGroups> wordGroups) {
        this.wordGroups = wordGroups;
        return this;
    }

    public List<String> getUserGroups() {
        return this.userGroups;
    }

    public ResultDefinition setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
        return this;
    }

    public List<Prompt> getPrompts() {
        return this.prompts;
    }

    public ResultDefinition setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public ResultDefinition setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public ResultDefinition setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ResultDefinition setVersion(String version) {
        this.version = version;
        return this;
    }

    public boolean isFinancial() {
        return financial != null && financial.equalsIgnoreCase(YES);
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

    public ResultDefinition setWelshLabel(String welshLabel) {
        this.welshLabel = welshLabel;
        return this;
    }
}
