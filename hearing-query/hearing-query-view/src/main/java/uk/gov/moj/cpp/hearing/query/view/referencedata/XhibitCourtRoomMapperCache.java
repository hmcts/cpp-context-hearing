package uk.gov.moj.cpp.hearing.query.view.referencedata;

import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XhibitCourtRoomMapperCache {

    @Inject
    private ReferenceDataCourtRoomService referenceDataCourtRoomService;

    private Map<String, CourtRoomMapping> eventMapperCache = new HashMap<>();

    public CourtRoomMapping getXhibitCourtRoomForCourtCentreAndRoomId(final UUID courtCentreId, final UUID courtRoomId) {
        final String courtCentreAndRoomKey = courtCentreId.toString().concat(courtRoomId.toString());
        return eventMapperCache.computeIfAbsent(courtCentreAndRoomKey, k -> referenceDataCourtRoomService.getCourtRoomNameBy(courtCentreId, courtRoomId));
    }
}
