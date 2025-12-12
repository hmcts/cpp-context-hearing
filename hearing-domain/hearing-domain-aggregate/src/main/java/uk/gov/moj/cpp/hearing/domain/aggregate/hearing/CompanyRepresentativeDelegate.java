package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeAdded;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeUpdated;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class CompanyRepresentativeDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public CompanyRepresentativeDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public Stream<Object> addCompanyRepresentative(final CompanyRepresentative companyRepresentative, final UUID hearingId) {
        if (this.momento.getCompanyRepresentatives().containsKey(companyRepresentative.getId())) {
            return Stream.of(new CompanyRepresentativeChangeIgnored(String.format("Provided company representative already exists, payload [%s]", companyRepresentative.toString())));
        }
        return Stream.of(new CompanyRepresentativeAdded(companyRepresentative, hearingId));
    }

    public void handleCompanyRepresentativeAdded(final CompanyRepresentativeAdded companyRepresentativeAdded) {
        final CompanyRepresentative companyRepresentative = companyRepresentativeAdded.getCompanyRepresentative();
        this.momento.getCompanyRepresentatives().put(companyRepresentative.getId(), companyRepresentative);
    }

    public Stream<Object> updateCompanyRepresentative(final CompanyRepresentative companyRepresentative, final UUID hearingId) {
        final Map<UUID, CompanyRepresentative> companyRepresentatives = this.momento.getCompanyRepresentatives();
        if (!companyRepresentatives.containsKey(companyRepresentative.getId())) {
            return Stream.of(new CompanyRepresentativeChangeIgnored(String.format("Provided company representative does not exists, payload [%s]", companyRepresentative.toString())));
        } else if (companyRepresentatives.get(companyRepresentative.getId()).equals(companyRepresentative)){
            return Stream.of(new CompanyRepresentativeChangeIgnored(String.format("No change in provided company representative, payload [%s]", companyRepresentative.toString())));
        }
        return Stream.of(new CompanyRepresentativeUpdated(companyRepresentative, hearingId));
    }

    public void handleCompanyRepresentativeUpdated(final CompanyRepresentativeUpdated companyRepresentativeUpdated) {
        final CompanyRepresentative companyRepresentative = companyRepresentativeUpdated.getCompanyRepresentative();
        this.momento.getCompanyRepresentatives().put(companyRepresentative.getId(), companyRepresentative);
    }

    public Stream<Object> removeCompanyRepresentative(final UUID id, final UUID hearingId) {
        final Map<UUID, CompanyRepresentative> companyRepresentatives = this.momento.getCompanyRepresentatives();
        if (!companyRepresentatives.containsKey(id)) {
            return Stream.of(new CompanyRepresentativeChangeIgnored(String.format("Provided company representative does not exists, payload [%s]", id.toString())));
        }
        return Stream.of(new CompanyRepresentativeRemoved(id, hearingId));
    }

    public void handleCompanyRepresentativeRemoved(final CompanyRepresentativeRemoved companyRepresentativeRemoved) {
        this.momento.getCompanyRepresentatives().remove(companyRepresentativeRemoved.getId());
    }
}
