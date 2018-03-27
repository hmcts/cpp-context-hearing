package uk.gov.moj.cpp.hearing.persist.entity.ex;

import java.time.LocalDateTime;
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
    @JoinColumns( {
            @JoinColumn(name = "defendant_id", insertable=false, updatable=false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable=false, updatable=false, referencedColumnName = "hearing_id")})
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
    private java.time.LocalDate startDate;

    @Column(name = "end_date")
    private java.time.LocalDate endDate;

    @Column(name = "conviction_date")
    private java.time.LocalDate convictionDate;
    
    @Column(name = "plea_id")
    private UUID pleaId;
    
    @Column(name = "plea_date")
    private LocalDateTime pleaDate;
    
    @Column(name = "plea_value")
    private String pleaValue;

    @Column(name = "verdict_id")
    private java.util.UUID verdictId;

    @Column(name = "verdict_code")
    private String verdictCode;

    @Column(name = "verdict_category")
    private String verdictCategory;

    @Column(name = "verdict_description")
    private String verdictDescription;

    @Column(name = "verdict_date")
    private java.time.LocalDateTime verdictDate;
    
    @Column(name = "number_of_jurors")
    private Integer numberOfJurors;
    
    @Column(name = "number_of_split_jurors")
    private Integer numberOfSplitJurors;
    
    @Column(name = "unanimous")
    private Boolean unanimous;

    //bi-directional many-to-one association to ACase
    @ManyToOne
    @JoinColumn(name="case_id")
    private LegalCase legalCase;

    @Column(name = "defendant_id")
    private UUID defendantId;

    public Offence() {

    }

    public Offence(Builder builder) {
        this.id = builder.id;
        this.defendant = builder.defendant;
        if (defendant!=null) {
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

    public java.time.LocalDate getStartDate() {
        return startDate;
    }

    public java.time.LocalDate getEndDate() {
        return endDate;
    }

    public java.time.LocalDate getConvictionDate() {
        return convictionDate;
    }

    public UUID getPleaId() {
        return pleaId;
    }

    public LocalDateTime getPleaDate() {
        return pleaDate;
    }

    public String getPleaValue() {
        return pleaValue;
    }

    public java.util.UUID getVerdictId() {
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

    public java.time.LocalDateTime getVerdictDate() {
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

    public static class Builder {

        private HearingSnapshotKey id;

        private LegalCase legalCase;

        private String code;

        private Integer count;

        private String wording;

        protected Builder() {}

        public String title;

        public String legislation;

        private java.time.LocalDate startDate;

        private java.time.LocalDate endDate;

        private java.time.LocalDate convictionDate;

        public UUID pleaId;

        public LocalDateTime pleaDate;
        
        public String pleaValue;

        private java.util.UUID verdictId;

        private String verdictCode;

        private String verdictCategory;

        private String verdictDescription;

        private java.time.LocalDateTime verdictDate;

        public Integer numberOfJurors;

        public Integer numberOfSplitJurors;

        public Boolean unanimous;

        private Defendant defendant;

        public Builder withId(final HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withCase(final LegalCase legalCase) {
            this.legalCase = legalCase;
            return this;
        }

        public Builder withDefendant(final Defendant defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withCode(final String code) {
            this.code = code;
            return this;
        }

        public Builder withCount(final Integer count) {
            this.count = count;
            return this;
        }

        public Builder withWording(final String wording) {
            this.wording = wording;
            return this;
        }

        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder withLegislation(final String legislation) {
            this.legislation = legislation;
            return this;
        }

        public Builder withStartDate(final java.time.LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(final java.time.LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withConvictionDate(final java.time.LocalDate convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }

        public Builder withPleaId(final java.util.UUID pleaId) {
            this.pleaId = pleaId;
            return this;
        }

        public Builder withPleaDate(final java.time.LocalDateTime pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }

        public Builder withPleaValue(final String pleaValue) {
            this.pleaValue = pleaValue;
            return this;
        }

        public Builder withVerdictId(final java.util.UUID verdictId) {
            this.verdictId = verdictId;
            return this;
        }

        public Builder withVerdictCode(final String verdictCode) {
            this.verdictCode = verdictCode;
            return this;
        }

        public Builder withVerdictCategory(final String verdictCategory) {
            this.verdictCategory = verdictCategory;
            return this;
        }

        public Builder withVerdictDescription(final String verdictDescription) {
            this.verdictDescription = verdictDescription;
            return this;
        }

        public Builder withVerdictDate(final java.time.LocalDateTime verdictDate) {
            this.verdictDate = verdictDate;
            return this;
        }

        public Builder withNumberOfJurors(final Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
            return this;
        }

        public Builder withNumberOfSplitJurors(final Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
            return this;
        }

        public Builder withUnanimous(final Boolean unanimous) {
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

}
