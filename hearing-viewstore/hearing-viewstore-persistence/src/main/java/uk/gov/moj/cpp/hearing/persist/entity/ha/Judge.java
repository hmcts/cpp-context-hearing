package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Judge")
public class Judge extends Attendee {

    public Judge() {
        super();
    }

    public Judge(final Judge.JudgeBuilder builder) {
        super(builder);
    }

    public static class JudgeBuilder extends Attendee.Builder {

        protected JudgeBuilder() {}

        @Override
        public Judge.JudgeBuilder withId(HearingSnapshotKey id) {
            super.withId(id);
            return this;
        }

        @Override
        public Judge build() {
             return new Judge(this);
         }

    }

    public static JudgeBuilder builder() {
        return new JudgeBuilder();
    }

}