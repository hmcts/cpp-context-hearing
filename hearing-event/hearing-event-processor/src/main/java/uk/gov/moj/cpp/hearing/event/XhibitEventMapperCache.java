package uk.gov.moj.cpp.hearing.event;

import uk.gov.moj.cpp.hearing.event.service.EventMapping;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XhibitEventMapperCache {

    @Inject
    private ReferenceDataService referenceDataService;

    private Map<String, String> eventMapperCache = new HashMap<>();

    @PostConstruct
    public void init() {
        final List<EventMapping> eventMapping = referenceDataService.getEventMapping();
        eventMapping.forEach(event -> eventMapperCache.put(event.getCppEventCode(),event.getXhibitEventCode()));
    }

    public String getXhibitEventCodeBy(final String cppEventCode){
        return eventMapperCache.get(cppEventCode);
    }
}
