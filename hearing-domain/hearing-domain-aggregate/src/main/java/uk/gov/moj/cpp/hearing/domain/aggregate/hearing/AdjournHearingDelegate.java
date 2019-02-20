package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;

import java.io.Serializable;
import java.util.stream.Stream;

@SuppressWarnings("squid:S1068")
public class AdjournHearingDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public AdjournHearingDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;

    }

    public Stream<Object> adjournHearing(final AdjournHearing adjournHearing) {
        // Start - This is a hack and this snippet of code will be removed and hearing language will be set in HearingAdjournTransformer once the language is made mandatory in initiate hearing..
        adjournHearing.getNextHearings().forEach(hearing -> {
            if (this.momento.getHearing().getHearingLanguage() == null) {
                hearing.setHearingLanguage(HearingLanguage.ENGLISH);
            }
            hearing.setHearingLanguage(this.momento.getHearing().getHearingLanguage());
        });
        // End - This is a hack and this snippet of code will be removed and hearing language will be set in HearingAdjournTransformer once the language is made mandatory in initiate hearing..
        HearingAdjourned hearingAdjourned = new HearingAdjourned(adjournHearing.getAdjournedHearing(), adjournHearing.getNextHearings());
        //TODO remove this bodge and update listing json schema
        hearingAdjourned.getNextHearings().forEach(hearing->hearing.getCourtCentre().setAddress(null));
        return Stream.of(hearingAdjourned);

    }
}
