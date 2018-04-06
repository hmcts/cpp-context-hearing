package uk.gov.moj.cpp.hearing.persist.entity.ex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "a_offence")
public class Offence {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "defendant_id", insertable = false, updatable = false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable = false, updatable = false, referencedColumnName = "hearing_id")})
    private Defendant defendant;

    @Column(name = "code")
    private String code;

    @Column(name = "count")
    private Integer count;

    @Column(name = "wording")
    private String wording;

    @Column(name = "title")
    private String title;

    @Column(name = "legislation")
    private String legislation;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "conviction_date")
    private LocalDate convictionDate;

    @Column(name = "plea_id")
    private UUID pleaId;

    @Column(name = "plea_date")
    private LocalDate pleaDate;

    @Column(name = "plea_value")
    private String pleaValue;

    @Column(name = "verdict_id")
    private UUID verdictId;

    @Column(name = "verdict_code")
    private String verdictCode;

    @Column(name = "verdict_category")
    private String verdictCategory;

    @Column(name = "verdict_description")
    private String verdictDescription;

    @Column(name = "verdict_date")
    private LocalDate verdictDate;

    @Column(name = "number_of_jurors")
    private Integer numberOfJurors;

    @Column(name = "number_of_split_jurors")
    private Integer numberOfSplitJurors;

    @Column(name = "unanimous")
    private Boolean unanimous;

    //bi-directional many-to-one association to ACase
    @ManyToOne
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;

    @Column(name = "defendant_id")
    private UUID defendantId;

    public Offence() {

    }

    public Offence(Builder builder) {
        this.id = builder.id;
        this.defendant = builder.defendant;
        if (defendant != null) {
            this.defendantId = builder.defendant.getId().getId();
        }
        this.code = builder.code;
        this.count = builder.count;
        this.wording = builder.wording;
        this.title = builder.title;
        this.legislation = builder.legislation;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.convictionDate = builder.convictionDate;
        this.pleaId = builder.pleaId;
        this.pleaDate = builder.pleaDate;
        this.pleaValue = builder.pleaValue;
        this.verdictId = builder.verdictId;
        this.verdictCode = builder.verdictCode;
        this.verdictCategory = builder.verdictCategory;
        this.verdictDescription = builder.verdictDescription;
        this.verdictDate = builder.verdictDate;
        this.numberOfJurors = builder.numberOfJurors;
        this.numberOfSplitJurors = builder.numberOfSplitJurors;
        this.unanimous = builder.unanimous;
        this.legalCase = builder.legalCase;
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public LegalCase getLegalCase() {
        return legalCase;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public String getCode() {
        return code;
    }

    public Integer getCount() {
        return count;
    }

    public String getWording() {
        return wording;
    }

    public String getTitle() {
        return title;
    }

    public String getLegislation() {
        return legislation;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public UUID getPleaId() {
        return pleaId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getPleaValue() {
        return pleaValue;
    }

    public UUID getVerdictId() {
        return verdictId;
    }

    public String getVerdictCode() {
        return verdictCode;
    }

    public String getVerdictCategory() {
        return verdictCategory;
    }

    public String getVerdictDescription() {
        return verdictDescription;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
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

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setVerdictId(UUID verdictId) {
        this.verdictId = verdictId;
    }

    public void setVerdictCode(String verdictCode) {
        this.verdictCode = verdictCode;
    }

    public void setVerdictCategory(String verdictCategory) {
        this.verdictCategory = verdictCategory;
    }

    public void setVerdictDescription(String verdictDescription) {
        this.verdictDescription = verdictDescription;
    }

    public void setVerdictDate(LocalDate verdictDate) {
        this.verdictDate = verdictDate;
    }

    public void setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
    }

    public void setNumberOfSplitJurors(Integer numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
    }

    public void setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
    }

    public void setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
    }

    public static class Builder {

        protected Builder() {
        }

        private HearingSnapshotKey id;

        private LegalCase legalCase;

        private String code;

        private Integer count;

        private String wording;

        private String title;

        private String legislation;

        private LocalDate startDate;

        private LocalDate endDate;

        private LocalDate convictionDate;

        private UUID pleaId;

        private LocalDate pleaDate;

        private String pleaValue;

        private UUID verdictId;

        private String verdictCode;

        private String verdictCategory;

        private String verdictDescription;

        private LocalDate verdictDate;

        private Integer numberOfJurors;

        private Integer numberOfSplitJurors;

        private Boolean unanimous;

        private Defendant defendant;

        public Builder withId(HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withCase(LegalCase legalCase) {
            this.legalCase = legalCase;
            return this;
        }

        public Builder withDefendant(Defendant defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withCount(Integer count) {
            this.count = count;
            return this;
        }

        public Builder withWording(String wording) {
            this.wording = wording;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withLegislation(String legislation) {
            this.legislation = legislation;
            return this;
        }

        public Builder withStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withConvictionDate(LocalDate convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }

        public Builder withPleaId(UUID pleaId) {
            this.pleaId = pleaId;
            return this;
        }

        public Builder withPleaDate(LocalDate pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }

        public Builder withPleaValue(String pleaValue) {
            this.pleaValue = pleaValue;
            return this;
        }

        public Builder withVerdictId(UUID verdictId) {
            this.verdictId = verdictId;
            return this;
        }

        public Builder withVerdictCode(String verdictCode) {
            this.verdictCode = verdictCode;
            return this;
        }

        public Builder withVerdictCategory(String verdictCategory) {
            this.verdictCategory = verdictCategory;
            return this;
        }

        public Builder withVerdictDescription(String verdictDescription) {
            this.verdictDescription = verdictDescription;
            return this;
        }

        public Builder withVerdictDate(LocalDate verdictDate) {
            this.verdictDate = verdictDate;
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

        public Offence build() {
            return new Offence(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((Offence) o).id);
    }
}