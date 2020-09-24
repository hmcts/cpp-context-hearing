package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;

import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;

public class JudgeNameMapper {
    private static final String CIRCUIT = "circuit";

    @Inject
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    private static final String TITLE_PREFIX = "titlePrefix";
    private static final String TITLE_JUDICIAL_PREFIX = "titleJudicialPrefix";
    private static final String TITLE_SUFFIX = "titleSuffix";
    private static final String FORENAMES = "forenames";
    private static final String SURNAME = "surname";
    private static final String REQUESTED_NAME = "requestedName";


    public String getJudgeName(final Hearing hearing) {
        final Optional<JudicialRole> judicialRole = hearing
                .getJudiciary()
                .stream()
                .filter(isCircuitJudge())
                .findFirst();

        if (judicialRole.isPresent()) {
            return getJudiciaryFullName(commonXhibitReferenceDataService.getJudiciary(judicialRole.get().getJudicialId()));
        }
        return EMPTY;
    }

    private String getJudiciaryFullName(final JsonObject judiciary) {

        final String requestedName = judiciary.getString(REQUESTED_NAME, EMPTY);
        if (isNotBlank(requestedName)) {
            return requestedName;
        }
        final String titlePrefix = judiciary.getString(TITLE_PREFIX, EMPTY);
        final String titleJudicialPrefix = judiciary.getString(TITLE_JUDICIAL_PREFIX, titlePrefix);
        final String foreNames = judiciary.getString(FORENAMES);
        final String sureName = judiciary.getString(SURNAME);
        final String titleSuffix = judiciary.getString(TITLE_SUFFIX, EMPTY);

        return format("%s %s %s %s", titleJudicialPrefix, foreNames, sureName, titleSuffix).trim();
    }

    private Predicate<JudicialRole> isCircuitJudge() {
        return hearingJudicialRole -> {
            final String judiciaryType = hearingJudicialRole.getJudicialRoleType().getJudiciaryType().toLowerCase();
            return (judiciaryType.contains(CIRCUIT));
        };
    }
}
