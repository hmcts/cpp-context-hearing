package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.UUID.fromString;

import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;
import uk.gov.moj.cpp.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.listing.domain.xhibit.CourtLocation;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class XhibitHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(XhibitHelper.class);

    @Inject
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    /**
     * As jurisdiction type is not available, try with crown court cache by court center Id if an
     * exception occurred then try with mags court cache to get court details and returns crest
     * court ID
     *
     * @param courtCentreId Court Center ID
     * @return Crest Court ID from cache
     */

    public String getCrestCourtId(final String courtCentreId) {

        CourtLocation courtDetails;
        final UUID courtCentreUUID = fromString(courtCentreId);

        try {
            courtDetails = commonXhibitReferenceDataService.getCrownCourtDetails(courtCentreUUID);
            return courtDetails.getCrestCourtId();
        } catch (final InvalidReferenceDataException referenceDataException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to find crest court center id {} in crown court reference cache so trying " +
                        "in mags court cache. Exception message is {}", courtCentreId, referenceDataException);
            }
            courtDetails = commonXhibitReferenceDataService.getMagsCourtDetails(courtCentreUUID);
            return courtDetails.getCrestCourtId();
        }
    }
}
