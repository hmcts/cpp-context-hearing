package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private UUID verdictId;
    private UUID verdictValueId;
    private String category;
    private String code;
    private String description;
    private Integer numberOfJurors;
    private Integer numberOfSplitJurors;
    private Boolean unanimous;
    private LocalDate verdictDate;

    @JsonIgnore
    public VerdictUpsert(Builder builder) {
        this.caseId = builder.caseId;
        this.hearingId = builder.hearingId;
        this.offenceId = builder.offenceId;
        this.verdictId = builder.verdictId;
        this.verdictValueId = builder.verdictValueId;
        this.category = builder.category;
        this.code = builder.code;
        this.description = builder.description;
        this.numberOfJurors = builder.numberOfJurors;
        this.numberOfSplitJurors = builder.numberOfSplitJurors;
        this.unanimous = builder.unanimous;
        this.verdictDate = builder.verdictDate;
    }

    @JsonCreator
    protected VerdictUpsert(@JsonProperty("caseId") UUID caseId,
                         @JsonProperty("hearingId") UUID hearingId,
                         @JsonProperty("offenceId") UUID offenceId,
                         @JsonProperty("verdictId") UUID verdictId,
                         @JsonProperty("verdictValueId") UUID verdictValueId,
                         @JsonProperty("category") String category,
                         @JsonProperty("code") String code,
                         @JsonProperty("description") String description,
                         @JsonProperty("numberOfJurors") Integer numberOfJurors,
                         @JsonProperty("numberOfSplitJurors") Integer numberOfSplitJurors,
                         @JsonProperty("unanimous") Boolean unanimous,
                         @JsonProperty("verdictDate") LocalDate verdictDate
    ) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.verdictId = verdictId;
        this.verdictValueId = verdictValueId;
        this.category = category;
        this.code = code;
        this.description = description;
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

    public UUID getVerdictId() {
        return verdictId;
    }

    public UUID getVerdictValueId() {
        return verdictValueId;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
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

    public static class Builder {
        private UUID caseId;
        private UUID hearingId;
        private UUID offenceId;
        private UUID verdictId;
        private UUID verdictValueId;
        private String category;
        private String code;
        private String description;
        private Integer numberOfJurors;
        private Integer numberOfSplitJurors;
        private Boolean unanimous;
        private LocalDate verdictDate;

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withOffenceId(UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withVerdictId(UUID verdictId) {
            this.verdictId = verdictId;
            return this;
        }

        public Builder withVerdictValueId(UUID verdictValueId) {
            this.verdictValueId = verdictValueId;
            return this;
        }

        public Builder withCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withNumberOfJurors(Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
            return this;
        }

        public Builder withNumberOfSplitJurors(Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
            return this;
        }

        public Builder withUnanimous(Boolean unanimous) {
            this.unanimous = unanimous;
            return this;
        }

        public Builder withVerdictDate(LocalDate verdictDate) {
            this.verdictDate = verdictDate;
            return this;
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

        public UUID getVerdictId() {
            return verdictId;
        }

        public UUID getVerdictValueId() {
            return verdictValueId;
        }

        public String getCategory() {
            return category;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
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

        public LocalDate getVerdictDate() {
            return verdictDate;
        }

        public VerdictUpsert build(){
            return new VerdictUpsert(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
