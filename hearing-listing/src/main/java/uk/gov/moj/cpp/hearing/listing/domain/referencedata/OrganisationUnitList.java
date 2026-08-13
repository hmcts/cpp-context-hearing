package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OrganisationUnitList {

    private List<OrganisationUnit> organisationunits;

    @JsonCreator
    public OrganisationUnitList(final List<OrganisationUnit> organisationunits) {
        this.organisationunits = organisationunits;
    }

    public List<OrganisationUnit> getOrganisationunits() {
        return organisationunits;
    }

    public void setOrganisationunits(final List<OrganisationUnit> organisationunits) {
        this.organisationunits = organisationunits;
    }
}