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
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse.Hearing;

/**
 * HearingDetailsResponseConverter. Deep conversion from a list of objects {@link Ahearing} to an object {@link HearingListResponse}
 */
public final class HearingListResponseConverter implements Converter<List<Ahearing>, HearingListResponse> {
    
    private static final HearingListResponseConverter HEARING_LIST_RESPONSE_CONVERTER = new HearingListResponseConverter();
    private static final HearingConverter HEARING_CONVERTER = new HearingConverter();
    
    /**
     * @param source
     * @return
     */
    public static HearingListResponse toHearingListResponse(final List<Ahearing> source) {
        return HEARING_LIST_RESPONSE_CONVERTER.convert(source);
    }
    
    /*
     * (non-Javadoc)
     * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public HearingListResponse convert(final List<Ahearing> source) {
        if (CollectionUtils.isEmpty(source)) {
            return new HearingListResponse();
        }
        return new HearingListResponse()
                .withHearings(source.stream().map(h -> convert(h)).collect(toList()));
    }

    /**
     * @param hearing
     * @return
     */
    private Hearing convert(final Ahearing hearing) {
        return HEARING_CONVERTER.convert(hearing);
    }
    
    // HearingConverter
    //-----------------------------------------------------------------------
    /**
     * HearingConverter. Converts an object {@link Ahearing} to an object {@link HearingListResponse.Hearing}
     */
    private static final class HearingConverter implements Converter<Ahearing, HearingListResponse.Hearing> {

        private static final DefendantConverter DEFENDANT_CONVERTER = new DefendantConverter();

        /*
         * (non-Javadoc)
         * @see uk.gov.justice.services.common.converter.Converter#convert(java.lang.Object)
         */
        @Override
        public HearingListResponse.Hearing convert(final Ahearing source) {
            if (null == source || null == source.getId()) {
                return null;
            }
            final List<Defendant> defendants = source.getDefendants();
            final Set<Offence> offences = defendants.stream().flatMap(d -> d.getOffences().stream()).collect(toSet());
            return new HearingListResponse.Hearing()
                    .withHearingId(source.getId().toString())
                    .withHearingType(source.getHearingType())
                    .withCaseUrn(offences.stream().map(o -> o.getLegalCase().getCaseurn()).collect(toList()))
                    .withDefendants(defendants.stream().map(convert()).collect(toList()));
        }

        /**
         * @return
         */
        private Function<? super Defendant, ? extends HearingListResponse.Defendant> convert() {
            return d -> DEFENDANT_CONVERTER.convert(d);
        }
        
    }
    
    // DefendantConverter
    //-----------------------------------------------------------------------
    /**
     * DefendantConverter. Converts an object {@link Defendant} to an object {@link HearingListResponse.Defendant} 
     */
    private static final class DefendantConverter implements Converter<Defendant, HearingListResponse.Defendant> {

        @Override
        public HearingListResponse.Defendant convert(final Defendant source) {
            if (null == source) {
                return null;
            }
            return new HearingListResponse.Defendant()
                    .withFirstName(source.getFirstName())
                    .withLastName(source.getLastName());
        }
    }
}
