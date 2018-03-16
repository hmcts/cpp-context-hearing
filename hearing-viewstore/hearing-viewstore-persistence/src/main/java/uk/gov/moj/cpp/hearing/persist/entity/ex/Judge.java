package uk.gov.moj.cpp.hearing.persist.entity.ex;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Judge")
/**
 * WARNING judge is not an Attendee in a business sense
 */
public class Judge extends Attendee {

    public Judge() {
        super();
    }

    public Judge(final Judge.Builder builder) {
        super(builder);
    }

    public static class Builder extends Attendee.Builder {
        
        public Judge.Builder withId(HearingSnapshotKey id) {
            super.withId(id);
            return this;
        }
        
        @SuppressWarnings("unchecked")
        public Judge build() {
            return new Judge(this);
        }
    }
}