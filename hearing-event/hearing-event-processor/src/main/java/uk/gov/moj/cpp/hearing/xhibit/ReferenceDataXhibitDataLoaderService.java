package uk.gov.moj.cpp.hearing.xhibit;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.service.EventMapping;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.courtcentre.CourtCentreCode;

import java.util.List;

public interface ReferenceDataXhibitDataLoaderService {

    List<EventMapping> getEventMapping();

    CourtCentreCode getXhibitCourtCentreCodeBy(final JsonEnvelope jsonEnvelope, final String courtCentreId);
}
