package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.util.UUID;

public class OrganisationUnit {

    private UUID id;

    private String oucode;

    public OrganisationUnit(final UUID id, final String oucode) {
        this.id = id;
        this.oucode = oucode;
    }

    public OrganisationUnit() {}

    public UUID getId() {
        return id;
    }

    public String getOucode() {
        return oucode;
    }

    public static class Builder {
        private UUID id;
        private String oucode;

        public OrganisationUnit.Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public OrganisationUnit.Builder withOucode(final String oucode) {
            this.oucode = oucode;
            return this;
        }

        public OrganisationUnit build() {
            return new OrganisationUnit(id, oucode);
        }
    }
}
