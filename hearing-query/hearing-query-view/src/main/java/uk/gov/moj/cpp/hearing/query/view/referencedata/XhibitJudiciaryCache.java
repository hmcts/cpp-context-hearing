package uk.gov.moj.cpp.hearing.query.view.referencedata;

import uk.gov.moj.cpp.hearing.query.view.service.ReferenceDataService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XhibitJudiciaryCache {

    @Inject
    private ReferenceDataService referenceDataService;

    private Map<UUID, String> judiciaryNameCache = new HashMap<>();

    public String getJudiciaryName(final UUID judiciaryId) {
        return judiciaryNameCache.computeIfAbsent(judiciaryId, k -> referenceDataService.getJudiciaryFullName(judiciaryId));
    }
}
