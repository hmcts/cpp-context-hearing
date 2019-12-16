package uk.gov.moj.cpp.hearing.query.view.referencedata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XhibitCourtRoomMapperCache {

    @Inject
    private ReferenceDataCourtRoomService referenceDataCourtRoomService;

    private Map<UUID, String> eventMapperCache = new HashMap<>();

    public String getXhibitCourtRoomName(final UUID courtCentreId, final UUID courtRoomId) {
        return eventMapperCache.computeIfAbsent(courtRoomId, k -> referenceDataCourtRoomService.getCourtRoomNameBy(courtCentreId, courtRoomId).getCrestCourtRoomName());
    }
}
