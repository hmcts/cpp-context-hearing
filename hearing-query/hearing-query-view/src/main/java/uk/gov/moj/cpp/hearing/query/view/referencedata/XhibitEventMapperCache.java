package uk.gov.moj.cpp.hearing.query.view.referencedata;

import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.query.view.service.ReferenceDataService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class XhibitEventMapperCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(XhibitEventMapperCache.class);
    @Inject
    private ReferenceDataService referenceDataService;

    private Map<UUID, XhibitEventMapping> eventMapperCache = new HashMap<>();

    @PostConstruct
    public void init() {
        final XhibitEventMappingsList eventMapping = referenceDataService.listAllEventMappings();
        if (eventMapping.getCpXhibitHearingEventMappings().isEmpty()) {
            LOGGER.warn("!!Xhibit Hearing Event Mapping is Empty!!");
        }

        eventMapping.getCpXhibitHearingEventMappings().forEach(event -> eventMapperCache.put(event.getCpHearingEventId(), event));
    }

    public Set<UUID> getCppHearingEventIds() {
        return eventMapperCache.keySet();
    }
}