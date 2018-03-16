package uk.gov.moj.cpp.hearing.persist.entity.ex;

import java.util.UUID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ProsecutionAdvocate")
public class ProsecutionAdvocate extends Advocate {

    public ProsecutionAdvocate() {
        super();
    }

    public ProsecutionAdvocate(final ProsecutionAdvocate.Builder builder) {
        super(builder);
    }

    public static class Builder extends Advocate.Builder {
        
        @Override
        public ProsecutionAdvocate.Builder withStatus(String status) {
            super.withStatus(status);
            return this;
        }

        @Override
        public ProsecutionAdvocate.Builder withId(HearingSnapshotKey id) {
            super.withId(id);
            return this;
        }

        @Override
        public ProsecutionAdvocate.Builder withPersonId(UUID personId) {
            super.withPersonId(personId);
            return this;
        }

        @Override
        public ProsecutionAdvocate.Builder withFirstName(String firstName) {
            super.withFirstName(firstName);
            return this;
        }

        @Override
        public ProsecutionAdvocate.Builder withLastName(String lastName) {
            super.withLastName(lastName);
            return this;
        }

        @Override
        public ProsecutionAdvocate.Builder withTitle(String title) {
            super.withTitle(title);
            return this;
        }

        public ProsecutionAdvocate build() {
             return new ProsecutionAdvocate(this);
         }
    }
}
