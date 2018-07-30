package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "ha_defendant_case")
public class DefendantCase {

    @EmbeddedId
    private DefendantCaseKey id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "defendant_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "hearing_id", referencedColumnName = "hearing_id", insertable = false, updatable = false)
    })
    private Defendant defendant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", insertable = false, updatable = false)
    private LegalCase legalCase;

    @Column(name = "bail_status")
    private String bailStatus;

    @Column(name = "custody_time_limit")
    private LocalDate custodyTimeLimitDate;

    public DefendantCase(Builder builder) {
        this.id = builder.id;
        this.hearing = builder.hearing;
        this.defendant = builder.defendant;
        this.legalCase = builder.legalCase;
        this.bailStatus = builder.bailStatus;
        this.custodyTimeLimitDate = builder.custodyTimeLimitDate;
    }

    public DefendantCase() {
    }

    public DefendantCaseKey getId() {
        return id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public void setDefendant(Defendant defendant) {
        this.defendant = defendant;
    }

    public LegalCase getLegalCase() {
        return legalCase;
    }

    public void setLegalCase(LegalCase legalCase) {
        this.legalCase = legalCase;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public void setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
    }

    public LocalDate getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public void setCustodyTimeLimitDate(LocalDate custodyTimeLimitDate) {
        this.custodyTimeLimitDate = custodyTimeLimitDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private DefendantCaseKey id;

        private Hearing hearing;

        private Defendant defendant;

        private LegalCase legalCase;

        private String bailStatus;

        private LocalDate custodyTimeLimitDate;

        public Builder withId(DefendantCaseKey id) {
            this.id = id;
            return this;
        }

        public Builder withHearing(Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withDefendant(Defendant defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withLegalCase(LegalCase legalCase) {
            this.legalCase = legalCase;
            return this;
        }

        public Builder withBailStatus(String bailStatus) {
            this.bailStatus = bailStatus;
            return this;
        }

        public Builder withCustodyTimeLimitDate(LocalDate custodyTimeLimitDate) {
            this.custodyTimeLimitDate = custodyTimeLimitDate;
            return this;
        }

        public DefendantCase build() {
            return new DefendantCase(this);
        }
    }
}
