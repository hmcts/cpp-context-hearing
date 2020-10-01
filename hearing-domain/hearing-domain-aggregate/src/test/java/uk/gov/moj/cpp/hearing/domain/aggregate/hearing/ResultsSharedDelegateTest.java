package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import org.junit.Test;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class ResultsSharedDelegateTest  {

    private final ResultsSharedDelegate resultsSharedDelegate = new ResultsSharedDelegate(null);


    @Test
    public void shouldReturnNullForNullHearing(){
        final Hearing hearing = resultsSharedDelegate.nullifyTheEmptyCaseMarkers(null);
        assertThat("Hearing not found null", hearing, is(nullValue()));
    }

    @Test
    public void shouldNotNullifyNonEmptyCaseMarkers(){
        final Hearing hearing = getHearingWithMarkers(true);
        resultsSharedDelegate.nullifyTheEmptyCaseMarkers(hearing);
        assertThat("Case markers  found null", resultsSharedDelegate.nullifyTheEmptyCaseMarkers(hearing).getProsecutionCases().get(0).getCaseMarkers(), is(notNullValue()));
    }

    @Test
    public void shouldNullifyEmptyCaseMarkers(){
        final Hearing hearing = getHearingWithMarkers(false);
        resultsSharedDelegate.nullifyTheEmptyCaseMarkers(hearing);
        assertThat("Case markers  found null", resultsSharedDelegate.nullifyTheEmptyCaseMarkers(hearing).getProsecutionCases().get(0).getCaseMarkers(), is(nullValue()));
    }

    @Test
    public void shouldReturnSameHearingObjectForNonNUllHearing(){
        final Hearing hearing = getHearingWithMarkers(true);
        assertThat("Not same haring instance", resultsSharedDelegate.nullifyTheEmptyCaseMarkers(hearing), sameInstance(hearing));
    }
    private Hearing getHearingWithMarkers(boolean nonZero) {
        final Marker marker = Marker.marker().withMarkerTypeCode("abc").build();
        final List<Marker> caseMarkers = new ArrayList<>();
        if(nonZero) {
            caseMarkers.add(marker);
        }
        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase().withCaseMarkers(caseMarkers).build();
        final List<ProsecutionCase> prosecutionCaseList = new ArrayList<>();
        prosecutionCaseList.add(prosecutionCase);
        final Hearing hearing = Hearing.hearing().withProsecutionCases(prosecutionCaseList).build();
        return hearing;
    }

}