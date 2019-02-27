package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseIdentifierJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseDefendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseHearing;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCase;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

public final class HearingListResponseConverter implements Converter<List<Hearing>, HearingListResponse> {

    @Override
    public HearingListResponse convert(final List<Hearing> source) {
        if (CollectionUtils.isEmpty(source)) {
            return new HearingListResponse();
        }
        return HearingListResponse.builder()
                .withHearings(source.stream().map(h -> new HearingConverter().convert(h)).collect(toList()))
                .build();
    }

    private static final class HearingConverter implements Converter<Hearing, HearingListResponseHearing> {

        @Inject
        private HearingTypeJPAMapper hearingTypeJPAMapper;

        @Inject
        private HearingDayJPAMapper hearingDayJPAMapper;

        @Inject
        private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper;

        @Override
        public HearingListResponseHearing convert(final Hearing source) {
            if (null == source || null == source.getId()) {
                return null;
            }

            final List<ProsecutionCase> prosecutionCases = source.getProsecutionCases().stream()
                    .map(prosecutionCase -> ProsecutionCase.builder()
                            .withId(prosecutionCase.getId().getId())
                            .withProsecutionCaseIdentifier(prosecutionCaseIdentifierJPAMapper.fromJPA(prosecutionCase.getProsecutionCaseIdentifier()))
                            .withDefendants(prosecutionCase.getDefendants().stream()
                                    .map(defendant -> HearingListResponseDefendant.builder()
                                            .withId(defendant.getId().getId())
                                            .withName(null).build()).collect(toList()))
                            .build()).collect(toList());

            return HearingListResponseHearing.builder()
                    .withId(source.getId())
                    .withType(hearingTypeJPAMapper.fromJPA(source.getHearingType()))
                    .withJurisdictionType(JurisdictionType.valueOf(source.getJurisdictionType().name()))
                    .withReportingRestrictionReason(source.getReportingRestrictionReason())
                    .withHearingLanguage(source.getHearingLanguage().name())
                    .withHearingDays(hearingDayJPAMapper.fromJPA(source.getHearingDays()))
                    .withProsecutionCases(prosecutionCases)
                    .withHasSharedResults(source.getHasSharedResults())
                    .build();
        }
    }
}
