package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Entity
@DiscriminatorValue("ADVOCATE")
public class Advocate extends Attendee {

    @Column(name = "status")
    private String status;
    
    @Transient
    private Set<LocalDate> hearingDates;

    public Advocate() {
        super();
    }

    public Advocate(final AdvocateBuilder advocateBuilder) {
        super(advocateBuilder);
        this.status = advocateBuilder.status;
    }

    public String getStatus() {
        return status;
    }

    public Advocate setStatus(String status) {
        this.status = status;
        return this;
    }

    public Set<LocalDate> getHearingDates() {
        return hearingDates;
    }

    public void addHearingDate(final LocalDate hearingDay) {
        if (null == hearingDates) {
            hearingDates = new TreeSet<>();
        }
        if (null != hearingDay) {
            this.hearingDates.add(hearingDay);
        }
    }
    public static class AdvocateBuilder extends Attendee.Builder {

        private String status;

        protected AdvocateBuilder() {
        }

        public AdvocateBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Advocate build() {
            return new Advocate(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Advocate advocate = (Advocate) o;
        return Objects.equals(this.getId(), advocate.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getId());
    }
}