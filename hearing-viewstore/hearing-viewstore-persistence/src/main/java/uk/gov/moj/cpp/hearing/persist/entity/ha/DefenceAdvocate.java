package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

@Entity
@DiscriminatorValue("DefenceAdvocate")
public class DefenceAdvocate extends Advocate {

    @ManyToMany(mappedBy = "defenceAdvocates", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Defendant> defendants = new ArrayList<>();

    public DefenceAdvocate() {

    }

    public DefenceAdvocate(final DefenceAdvocateAdvocateBuilder builder) {
        super(builder);
        this.defendants = builder.defendants;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public void setDefendants(final List<Defendant> defendants) {
        this.defendants = defendants;
    }

    public static class DefenceAdvocateAdvocateBuilder extends AdvocateBuilder {

        private List<Defendant> defendants = new ArrayList<>();

        protected DefenceAdvocateAdvocateBuilder() {
        }

        @Override
        public DefenceAdvocateAdvocateBuilder withStatus(String status) {
            super.withStatus(status);
            return this;
        }

        @Override
        public DefenceAdvocateAdvocateBuilder withId(HearingSnapshotKey id) {
            super.withId(id);
            return this;
        }

        @Override
        public DefenceAdvocateAdvocateBuilder withPersonId(UUID personId) {
            super.withPersonId(personId);
            return this;
        }

        @Override
        public DefenceAdvocateAdvocateBuilder withFirstName(String firstName) {
            super.withFirstName(firstName);
            return this;
        }

        @Override
        public DefenceAdvocateAdvocateBuilder withLastName(String lastName) {
            super.withLastName(lastName);
            return this;
        }

        @Override
        public DefenceAdvocateAdvocateBuilder withTitle(String title) {
            super.withTitle(title);
            return this;
        }


        public DefenceAdvocateAdvocateBuilder addDefendant(Defendant defendant) {
            this.defendants.add(defendant);
            return this;
        }

        @Override
        public DefenceAdvocate build() {
            return new DefenceAdvocate(this);
        }
    }

    public static DefenceAdvocateAdvocateBuilder builder() {
        return new DefenceAdvocateAdvocateBuilder();
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