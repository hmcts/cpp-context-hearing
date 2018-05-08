package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@DiscriminatorValue("ADVOCATE")
public class Advocate extends Attendee {

    @Column(name = "status")
    private String status;

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