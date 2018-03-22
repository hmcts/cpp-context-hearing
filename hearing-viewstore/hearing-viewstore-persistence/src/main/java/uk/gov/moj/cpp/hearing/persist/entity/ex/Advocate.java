package uk.gov.moj.cpp.hearing.persist.entity.ex;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ADVOCATE")
/**
 * Advocates may be linked as defence counsel or linked as
 */
public class Advocate extends Attendee {

    @Column(name = "status")
    private String status;

    public Advocate() {
        super();
    }

    public Advocate(final Advocate.Builder builder) {
        super(builder);
        this.status = builder.status;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status=status;
    }

    public static class Builder extends Attendee.Builder {

        private String status;

        protected Builder() {}

        public Advocate.Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Advocate build() {
             return new Advocate(this);
         }

    }
}