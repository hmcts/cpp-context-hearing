package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;

import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedDefenceOrganisation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AssociatedDefenceOrganisationJPAMapper {
    private DefenceOrganisationJPAMapper defenceOrganisationJPAMapper;

    @Inject
    public AssociatedDefenceOrganisationJPAMapper(final DefenceOrganisationJPAMapper defenceOrganisationJPAMapper) {
        this.defenceOrganisationJPAMapper = defenceOrganisationJPAMapper;
    }

    public AssociatedDefenceOrganisationJPAMapper() {
    }

    public AssociatedDefenceOrganisation toJPA(final uk.gov.justice.core.courts.AssociatedDefenceOrganisation associatedDefenceOrganisationPojo) {
        if (isNull(associatedDefenceOrganisationPojo)) {
            return null;
        }

        final AssociatedDefenceOrganisation associatedDefenceOrganisation = new AssociatedDefenceOrganisation();
        associatedDefenceOrganisation.setApplicationReference(associatedDefenceOrganisationPojo.getApplicationReference());
        associatedDefenceOrganisation.setAssociationEndDate(associatedDefenceOrganisationPojo.getAssociationEndDate());
        associatedDefenceOrganisation.setAssociationStartDate(associatedDefenceOrganisationPojo.getAssociationStartDate());
        associatedDefenceOrganisation.setAssociatedByLAA(associatedDefenceOrganisationPojo.getIsAssociatedByLAA());
        associatedDefenceOrganisation.setFundingType(associatedDefenceOrganisationPojo.getFundingType());
        associatedDefenceOrganisation.setDefenceOrganisation(defenceOrganisationJPAMapper.toJPA(associatedDefenceOrganisationPojo.getDefenceOrganisation()));
        return associatedDefenceOrganisation;
    }

    public uk.gov.justice.core.courts.AssociatedDefenceOrganisation fromJPA(final AssociatedDefenceOrganisation associatedDefenceOrganisation) {
        if (isNull(associatedDefenceOrganisation)) {
            return null;
        }

        return new uk.gov.justice.core.courts.AssociatedDefenceOrganisation(
                associatedDefenceOrganisation.getApplicationReference(),
                associatedDefenceOrganisation.getAssociationEndDate(),
                associatedDefenceOrganisation.getAssociationStartDate(),
                defenceOrganisationJPAMapper.fromJPA(associatedDefenceOrganisation.getDefenceOrganisation()),
                associatedDefenceOrganisation.getFundingType(),
                associatedDefenceOrganisation.getAssociatedByLAA()
        );
    }
}
