package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.offence-verdict-updated")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerdictUpsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID caseId;
    private UUID hearingId;
    private UUID offenceId;
    private UUID verdictTypeId;
    private String category;
    private String categoryType;
    private UUID offenceDefinitionId;
    private String title;
    private String offenceCode;
    private String legislation;
    private Integer numberOfJurors;
    private Integer numberOfSplitJurors;
    private Boolean unanimous;
    private LocalDate verdictDate;

    public VerdictUpsert() {
    }

    @JsonCreator
    protected VerdictUpsert(@JsonProperty("caseId") final UUID caseId,
                            @JsonProperty("hearingId") final UUID hearingId,
                            @JsonProperty("offenceId") final UUID offenceId,
                            @JsonProperty("verdictTypeId") final UUID verdictTypeId,
                            @JsonProperty("category") final String category,
                            @JsonProperty("categoryType") final String categoryType,
                            @JsonProperty("offenceDefinitionId") final UUID offenceDefinitionId,
                            @JsonProperty("title") final String lesserOffence,
                            @JsonProperty("offenceCode") final String code,
                            @JsonProperty("legislation") final String description,
                            @JsonProperty("numberOfJurors") final Integer numberOfJurors,
                            @JsonProperty("numberOfSplitJurors") final Integer numberOfSplitJurors,
                            @JsonProperty("unanimous") final Boolean unanimous,
                            @JsonProperty("verdictDate") final LocalDate verdictDate) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.verdictTypeId = verdictTypeId;
        this.category = category;
        this.categoryType = categoryType;
        this.offenceDefinitionId = offenceDefinitionId;
        this.title = lesserOffence;
        this.offenceCode = code;
        this.legislation = description;
        this.numberOfJurors = numberOfJurors;
        this.numberOfSplitJurors = numberOfSplitJurors;
        this.unanimous = unanimous;
        this.verdictDate = verdictDate;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getVerdictTypeId() {
        return verdictTypeId;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public UUID getOffenceDefinitionId() {
        return offenceDefinitionId;
    }

    public String getTitle() {
        return title;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public String getLegislation() {
        return legislation;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public VerdictUpsert setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public VerdictUpsert setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public VerdictUpsert setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public VerdictUpsert setVerdictTypeId(UUID verdictTypeId) {
        this.verdictTypeId = verdictTypeId;
        return this;
    }

    public VerdictUpsert setCategory(String category) {
        this.category = category;
        return this;
    }

    public VerdictUpsert setCategoryType(String categoryType) {
        this.categoryType = categoryType;
        return this;
    }

    public VerdictUpsert setOffenceDefinitionId(UUID offenceDefinitionId) {
        this.offenceDefinitionId = offenceDefinitionId;
        return this;
    }

    public VerdictUpsert setTitle(String title) {
        this.title = title;
        return this;
    }

    public VerdictUpsert setOffenceCode(String offenceCode) {
        this.offenceCode = offenceCode;
        return this;
    }

    public VerdictUpsert setLegislation(String legislation) {
        this.legislation = legislation;
        return this;
    }

    public VerdictUpsert setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
        return this;
    }

    public VerdictUpsert setNumberOfSplitJurors(Integer numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
        return this;
    }

    public VerdictUpsert setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
        return this;
    }

    public VerdictUpsert setVerdictDate(LocalDate verdictDate) {
        this.verdictDate = verdictDate;
        return this;
    }

    public static VerdictUpsert verdictUpsert() {
        return new VerdictUpsert();
    }

}
