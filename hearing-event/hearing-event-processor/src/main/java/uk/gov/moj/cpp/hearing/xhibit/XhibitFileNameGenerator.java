package uk.gov.moj.cpp.hearing.xhibit;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.fromString;

import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class XhibitFileNameGenerator {

    private static final String WEB_PAGE_PREFIX = "WebPage";
    private static final String PUBLIC_DISPLAY_PAGE_PREFIX = "PublicDisplay";

    @Inject
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    public String generateWebPageFileName(final ZonedDateTime requestedDate, final String courtCentreId) {
        return generateFileName(WEB_PAGE_PREFIX, requestedDate, courtCentreId);
    }

    public String generatePublicDisplayFileName(final ZonedDateTime requestedDate, final String courtCentreId) {
        return generateFileName(PUBLIC_DISPLAY_PAGE_PREFIX, requestedDate, courtCentreId);
    }

    private String generateFileName(final String prefix, final ZonedDateTime requestedDate, final String courtCentreId) {

        final String xhibitCourtCentreCode = getCourtCode(courtCentreId);

        return format("%s_%s_%s.xml", prefix, xhibitCourtCentreCode, getSendDate(requestedDate));
    }

    private String getSendDate(final ZonedDateTime createdDate) {
        final DateTimeFormatter formatter = ofPattern("uuuuMMddHHmmss");
        return createdDate.format(formatter);
    }

    private String getCourtCode(final String courtCentreId) {
        return commonXhibitReferenceDataService.getCourtDetails(fromString(courtCentreId)).getCrestCourtId();
    }
}
