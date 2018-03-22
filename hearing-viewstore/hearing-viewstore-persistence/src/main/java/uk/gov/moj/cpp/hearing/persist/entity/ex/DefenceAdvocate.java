package uk.gov.moj.cpp.hearing.persist.entity.ex;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

@Entity
@DiscriminatorValue("DefenceAdvocate")
public class DefenceAdvocate extends Advocate {

    @ManyToMany(mappedBy = "defenceAdvocates")
    private List<Defendant> defendants=new ArrayList<>();

    public DefenceAdvocate() {

    }
    public DefenceAdvocate(final DefenceAdvocate.Builder builder) {
        super(builder);
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public void setDefendants(final List<Defendant> defendants) {
        this.defendants = defendants;
    }

    public static class Builder extends Advocate.Builder {

        @Override
        public DefenceAdvocate.Builder withStatus(String status) {
            super.withStatus(status);
            return this;
        }

        @Override
        public DefenceAdvocate.Builder withId(HearingSnapshotKey id) {
            super.withId(id);
            return this;
        }

        @Override
        public DefenceAdvocate.Builder withPersonId(UUID personId) {
            super.withPersonId(personId);
            return this;
        }

        @Override
        public DefenceAdvocate.Builder withFirstName(String firstName) {
            super.withFirstName(firstName);
            return this;
        }

        @Override
        public DefenceAdvocate.Builder withLastName(String lastName) {
            super.withLastName(lastName);
            return this;
        }

        @Override
        public DefenceAdvocate.Builder withTitle(String title) {
            super.withTitle(title);
            return this;
        }


        protected Builder() {}
        @Override
        public DefenceAdvocate build() {
            return new DefenceAdvocate(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}