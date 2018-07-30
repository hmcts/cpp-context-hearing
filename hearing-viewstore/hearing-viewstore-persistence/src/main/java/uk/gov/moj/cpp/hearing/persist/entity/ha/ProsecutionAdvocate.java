package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ProsecutionAdvocate")
public class ProsecutionAdvocate extends Advocate {

    public ProsecutionAdvocate() {
        super();
    }

    public ProsecutionAdvocate(final ProsecutionAdvocateAdvocateBuilder builder) {
        super(builder);
    }

    public static class ProsecutionAdvocateAdvocateBuilder extends AdvocateBuilder {

        protected ProsecutionAdvocateAdvocateBuilder() {
        }

        @Override
        public ProsecutionAdvocateAdvocateBuilder withStatus(String status) {
            super.withStatus(status);
            return this;
        }

        @Override
        public ProsecutionAdvocateAdvocateBuilder withId(HearingSnapshotKey id) {
            super.withId(id);
            return this;
        }

        @Override
        public ProsecutionAdvocateAdvocateBuilder withPersonId(UUID personId) {
            super.withPersonId(personId);
            return this;
        }

        @Override
        public ProsecutionAdvocateAdvocateBuilder withFirstName(String firstName) {
            super.withFirstName(firstName);
            return this;
        }

        @Override
        public ProsecutionAdvocateAdvocateBuilder withLastName(String lastName) {
            super.withLastName(lastName);
            return this;
        }

        @Override
        public ProsecutionAdvocateAdvocateBuilder withTitle(String title) {
            super.withTitle(title);
            return this;
        }

        @Override
        public ProsecutionAdvocate build() {
            return new ProsecutionAdvocate(this);
        }
    }

    public static ProsecutionAdvocateAdvocateBuilder builder() {
        return new ProsecutionAdvocateAdvocateBuilder();
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
