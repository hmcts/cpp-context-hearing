package uk.gov.moj.cpp.hearing.xhibit.refdatacache;

import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.xhibit.ReferenceDataXhibitDataLoader;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XhibitEventMapperCache {

    @Inject
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;

    private Map<String, String> eventMapperCache = new HashMap<>();

    @PostConstruct
    public void init() {
        final XhibitEventMappingsList eventMapping = referenceDataXhibitDataLoader.getEventMapping();
        eventMapping.getCpXhibitHearingEventMappings().forEach(event -> eventMapperCache.put(event.getCpHearingEventId().toString(), event.getXhibitHearingEventCode()));
    }

    public String getXhibitEventCodeBy(final String cppEventCode) {
        return eventMapperCache.get(cppEventCode);
    }
}
