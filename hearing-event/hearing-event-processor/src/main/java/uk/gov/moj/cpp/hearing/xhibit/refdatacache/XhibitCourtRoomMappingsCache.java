package uk.gov.moj.cpp.hearing.xhibit.refdatacache;

import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.xhibit.ReferenceDataXhibitDataLoader;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XhibitCourtRoomMappingsCache {

    @Inject
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;

    private Map<String, CourtRoomMappingsList> courtRoomMappingsCache = new HashMap<>();

    public CourtRoomMappingsList getCourtRoomMappingsMapCache(final String courtCentreId) {
        return courtRoomMappingsCache.computeIfAbsent(courtCentreId, key -> referenceDataXhibitDataLoader.getCourtRoomMappingsList(courtCentreId));
    }
}
