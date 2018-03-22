package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponseDefendant;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponseHearing;

public final class HearingListResponseConverter implements Converter<List<Ahearing>, HearingListResponse> {
    
    private static final HearingListResponseConverter HEARING_LIST_RESPONSE_CONVERTER = new HearingListResponseConverter();
    private static final HearingConverter HEARING_CONVERTER = new HearingConverter();
    
    public static HearingListResponse toHearingListResponse(final List<Ahearing> source) {
        return HEARING_LIST_RESPONSE_CONVERTER.convert(source);
    }
    
    @Override
    public HearingListResponse convert(final List<Ahearing> source) {
        if (CollectionUtils.isEmpty(source)) {
            return null;
        }
        return HearingListResponse.builder()
                .withHearings(source.stream().map(h -> convert(h)).collect(toList()))
                .build();
    }

    private HearingListResponseHearing convert(final Ahearing hearing) {
        return HEARING_CONVERTER.convert(hearing);
    }
    
    // HearingConverter
    //-----------------------------------------------------------------------
    private static final class HearingConverter implements Converter<Ahearing, HearingListResponseHearing> {

        private static final DefendantConverter DEFENDANT_CONVERTER = new DefendantConverter();

        @Override
        public HearingListResponseHearing convert(final Ahearing source) {
            if (null == source || null == source.getId()) {
                return null;
            }
            final List<Defendant> defendants = source.getDefendants();
            final Set<Offence> offences = defendants.stream().flatMap(d -> d.getOffences().stream()).collect(toSet());
            return HearingListResponseHearing.builder()
                    .withHearingId(source.getId().toString())
                    .withHearingType(source.getHearingType())
                    .withCaseUrn(offences.stream().map(o -> o.getLegalCase().getCaseurn()).collect(toList()))
                    .withDefendants(defendants.stream().map(convert()).collect(toList()))
                    .build();
        }

        private Function<? super Defendant, ? extends HearingListResponseDefendant> convert() {
            return d -> DEFENDANT_CONVERTER.convert(d);
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