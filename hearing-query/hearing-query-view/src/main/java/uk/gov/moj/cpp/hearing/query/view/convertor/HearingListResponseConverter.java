package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponseDefendant;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponseHearing;
import uk.gov.moj.cpp.hearing.query.view.response.LegalCase;

public final class HearingListResponseConverter implements Converter<List<Ahearing>, HearingListResponse> {
    
    @Override
    public HearingListResponse convert(final List<Ahearing> source) {
        if (CollectionUtils.isEmpty(source)) {
            return new HearingListResponse();
        }
        return HearingListResponse.builder()
                .withHearings(source.stream().map(h -> new HearingConverter().convert(h)).collect(toList()))
                .build();
    }

    // HearingConverter
    //-----------------------------------------------------------------------
    private static final class HearingConverter implements Converter<Ahearing, HearingListResponseHearing> {

        @Override
        public HearingListResponseHearing convert(final Ahearing source) {
            if (null == source || null == source.getId()) {
                return null;
            }
            final List<Defendant> defendants = source.getDefendants();
            final List<uk.gov.moj.cpp.hearing.query.view.response.LegalCase> cases = defendants.stream()
                    .flatMap(d -> d.getOffences().stream())
                    .map(Offence::getLegalCase)
                    .distinct()
                    .map(legalCase -> LegalCase.builder()
                            .withId(legalCase.getId())
                            .withUrn(legalCase.getCaseUrn())
                            .build()
                    )
                    .collect(toList());

            return HearingListResponseHearing.builder()
                    .withHearingId(source.getId().toString())
                    .withHearingType(source.getHearingType())
                    .withCases(cases)
                    .withDefendants(defendants.stream()
                            .map(d -> new DefendantConverter().convert(d))
                            .collect(toList()))
                    .build();
        }
    }

    // DefendantConverter
    //-----------------------------------------------------------------------
    private static final class DefendantConverter implements Converter<Defendant, HearingListResponseDefendant> {

        @Override
        public HearingListResponseDefendant convert(final Defendant source) {
            if (null == source) {
                return null;
            }
            return HearingListResponseDefendant.builder()
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName())
                    .build();
        }
    }
}