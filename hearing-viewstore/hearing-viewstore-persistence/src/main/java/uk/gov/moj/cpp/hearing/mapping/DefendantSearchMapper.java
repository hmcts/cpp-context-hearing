package uk.gov.moj.cpp.hearing.mapping;

import org.apache.deltaspike.data.api.mapping.SimpleQueryInOutMapperBase;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;

import java.util.Optional;

public class DefendantSearchMapper extends SimpleQueryInOutMapperBase<Defendant, DefendantSearch> {
    @Override
    protected Object getPrimaryKey(final DefendantSearch defendantSearch) {
        return null;
    }

    @Override
    protected DefendantSearch toDto(final Defendant defendant) {
        final DefendantSearch defendantSearch = new DefendantSearch();
        defendantSearch.setDefendantId(defendant.getId().getId());
        Optional.ofNullable(defendant.getLegalEntityOrganisation())
                                        .map(Organisation::getName)
                                         .ifPresent(defendantSearch::setLegalEntityOrganizationName);
        Optional.ofNullable(defendant.getPersonDefendant())
                                        .map(PersonDefendant::getPersonDetails)
                                        .ifPresent(person -> this.setPersonDetails(defendantSearch, person));

        return defendantSearch;
    }

    @Override
    protected Defendant toEntity(final Defendant defendant, final DefendantSearch defendantSearch) {
        return defendant;
    }


    private void setPersonDetails(final DefendantSearch defendantSearch, final Person person) {
        defendantSearch.setDateOfBirth(person.getDateOfBirth());
        defendantSearch.setNationalInsuranceNumber(person.getNationalInsuranceNumber());
        defendantSearch.setForename(person.getFirstName());
        defendantSearch.setSurname(person.getLastName());
    }
}
