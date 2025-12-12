package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.UUID;

@Event("hearing.company-representative-added")
public class CompanyRepresentativeAdded implements Serializable {

    private static final long serialVersionUID = -8149823644051387630L;

    private final CompanyRepresentative companyRepresentative;
    private final UUID hearingId;

    public CompanyRepresentativeAdded(final CompanyRepresentative companyRepresentative, final UUID hearingId) {
        this.companyRepresentative = companyRepresentative;
        this.hearingId = hearingId;
    }

    public CompanyRepresentative getCompanyRepresentative() {
        return companyRepresentative;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
