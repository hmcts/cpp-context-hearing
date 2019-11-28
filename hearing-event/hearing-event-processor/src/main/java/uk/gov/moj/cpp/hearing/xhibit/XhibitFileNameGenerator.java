package uk.gov.moj.cpp.hearing.xhibit;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.xhibit.pojo.CourtCentreCode;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class XhibitFileNameGenerator {

    private static final String WEB_PAGE_PREFIX = "WebPage";
    private static final String PUBLIC_DISPLAY_PAGE_PREFIX = "PD";

    @Inject
    private ReferenceDataXhibitDataLoaderService referenceDataXhibitDataLoaderService;

    public String generateWebPageFileName(final JsonEnvelope jsonEnvelope, final ZonedDateTime requestedDate, final String courtCentreId) {
        return generateFileName(jsonEnvelope, WEB_PAGE_PREFIX, requestedDate, courtCentreId);
    }

    public String generatePublicDisplayFileName(final JsonEnvelope jsonEnvelope, final ZonedDateTime requestedDate, final String courtCentreId) {
        return generateFileName(jsonEnvelope, PUBLIC_DISPLAY_PAGE_PREFIX, requestedDate, courtCentreId);
    }

    private String generateFileName(final JsonEnvelope jsonEnvelope, final String prefix, final ZonedDateTime requestedDate, final String courtCentreId) {

        final CourtCentreCode xhibitCourtCentreCode = getCourtCode(jsonEnvelope, courtCentreId);

        final String filename = String.format("%s_%s_%s.xml", prefix, xhibitCourtCentreCode.getCrestCodeId(), getSendDate(requestedDate));

        return filename;
    }

    private String getSendDate(final ZonedDateTime createdDate) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return createdDate.format(formatter);
    }


    private CourtCentreCode getCourtCode(final JsonEnvelope jsonEnvelope, final String courtCentreId) {
        return referenceDataXhibitDataLoaderService.getXhibitCourtCentreCodeBy(jsonEnvelope, courtCentreId);
    }
}
