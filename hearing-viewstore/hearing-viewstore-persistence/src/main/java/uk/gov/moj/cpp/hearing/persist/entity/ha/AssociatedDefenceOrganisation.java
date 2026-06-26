package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.core.courts.FundingType;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@SuppressWarnings("squid:S1186")
@Embeddable
public class AssociatedDefenceOrganisation {
    @Column(name = "association_start_date")
    private LocalDate associationStartDate;

    @Column(name = "association_end_date")
    private LocalDate associationEndDate;

    @Column(name = "funding_type")
    @Enumerated(EnumType.STRING)
    private FundingType fundingType;

    @Column(name = "is_associated_by_laa")
    private Boolean isAssociatedByLAA;

    @Column(name = "application_reference")
    private String applicationReference;

    @Embedded
    private DefenceOrganisation defenceOrganisation;

    public AssociatedDefenceOrganisation() {
    }

    public LocalDate getAssociationStartDate() {
        return associationStartDate;
    }

    public void setAssociationStartDate(final LocalDate associationStartDate) {
        this.associationStartDate = associationStartDate;
    }

    public LocalDate getAssociationEndDate() {
        return associationEndDate;
    }

    public void setAssociationEndDate(final LocalDate associationEndDate) {
        this.associationEndDate = associationEndDate;
    }

    public FundingType getFundingType() {
        return fundingType;
    }

    public void setFundingType(final FundingType fundingType) {
        this.fundingType = fundingType;
    }

    public Boolean getAssociatedByLAA() {
        return isAssociatedByLAA;
    }

    public void setAssociatedByLAA(final Boolean associatedByLAA) {
        isAssociatedByLAA = associatedByLAA;
    }

    public DefenceOrganisation getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public void setDefenceOrganisation(final DefenceOrganisation defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
    }

    public String getApplicationReference() {
        return applicationReference;
    }

    public void setApplicationReference(final String applicationReference) {
        this.applicationReference = applicationReference;
    }
}
