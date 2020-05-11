package uk.gov.moj.cpp.hearing.query.view.referencedata;

import static java.lang.String.format;

import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class XhibitJudiciaryCache {

    @Inject
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    private static final String TITLE_PREFIX = "titlePrefix";
    private static final String TITLE_JUDICIAL_PREFIX = "titleJudicialPrefix";
    private static final String TITLE_SUFFIX = "titleSuffix";
    private static final String FORENAMES = "forenames";
    private static final String SURNAME = "surname";

    private Map<UUID, String> judiciaryNameCache = new HashMap<>();

    public String getJudiciaryName(final UUID judiciaryId) {
        return judiciaryNameCache.computeIfAbsent(judiciaryId, k ->
            getJudiciaryFullName(commonXhibitReferenceDataService.getJudiciary(judiciaryId))
        );
    }

    private String getJudiciaryFullName(final JsonObject judiciary) {

        final String titlePrefix = judiciary.getString(TITLE_PREFIX, StringUtils.EMPTY);
        final String titleJudicialPrefix = judiciary.getString(TITLE_JUDICIAL_PREFIX, titlePrefix);
        final String foreNames = judiciary.getString(FORENAMES);
        final String sureName = judiciary.getString(SURNAME);
        final String titleSuffix = judiciary.getString(TITLE_SUFFIX, StringUtils.EMPTY);

        return format("%s %s %s %s", titleJudicialPrefix, foreNames, sureName, titleSuffix).trim();
    }
}
