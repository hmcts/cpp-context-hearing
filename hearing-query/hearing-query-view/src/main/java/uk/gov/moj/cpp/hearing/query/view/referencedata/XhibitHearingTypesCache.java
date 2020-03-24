package uk.gov.moj.cpp.hearing.query.view.referencedata;

import uk.gov.moj.cpp.external.domain.referencedata.HearingTypeMappingList;
import uk.gov.moj.cpp.hearing.query.view.service.ReferenceDataService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class XhibitHearingTypesCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(XhibitHearingTypesCache.class);

    @Inject
    private ReferenceDataService referenceDataService;

    private Map<UUID, String> hearingTypeCache = new HashMap<>();

    @PostConstruct
    public  void init(){
        final HearingTypeMappingList hearingTypeMappingList = referenceDataService.getXhibitHearingType();

        if (hearingTypeMappingList.getHearingTypes().isEmpty()) {
            LOGGER.warn("!!Xhibit Hearing Types Mapping is Empty!!");
        }

        hearingTypeMappingList.getHearingTypes().forEach(hearingTypeMapping -> hearingTypeCache.put(hearingTypeMapping.getId(), hearingTypeMapping.getExhibitHearingDescription()));
    }

    public String getHearingTypeDescription(final UUID cppHearingTypeId){
        return hearingTypeCache.get(cppHearingTypeId);
    }
}
