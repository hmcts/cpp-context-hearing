package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeAdded;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeUpdated;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

public class CompanyRepresentativeDelegateTest {

    @Test
    public void shouldAddCompanyRepresentative(){
        final UUID hearingId = randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(randomUUID(), new CompanyRepresentative.Builder().withId(randomUUID()).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        final Stream<Object> objectStream = companyRepresentativeDelegate.addCompanyRepresentative(
                new CompanyRepresentative.Builder().withId(randomUUID()).build()
                , hearingId);
        final CompanyRepresentativeAdded companyRepresentativeAdded = (CompanyRepresentativeAdded)
                objectStream.collect(Collectors.toList()).get(0);
        assertThat(companyRepresentativeAdded.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldNotAddCompanyRepresentativeIfMomentoHasSameCompanyRepresentative(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(companyRepId, new CompanyRepresentative.Builder().withId(randomUUID()).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        final Stream<Object> objectStream = companyRepresentativeDelegate.addCompanyRepresentative(
                new CompanyRepresentative.Builder().withId(companyRepId).build()
                , hearingId);
        final CompanyRepresentativeChangeIgnored companyRepresentativeChangeIgnored = (CompanyRepresentativeChangeIgnored)
                objectStream.collect(Collectors.toList()).get(0);
        assertThat(companyRepresentativeChangeIgnored.getReason(), is("Provided company representative already exists, payload ["+
                new CompanyRepresentative.Builder().withId(companyRepId).build().toString()+"]"));
    }

    @Test
    public void shouldHandleCompanyRepresentativeAdded(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        companyRepresentativeDelegate.handleCompanyRepresentativeAdded(
                new CompanyRepresentativeAdded(new CompanyRepresentative.Builder().withId(companyRepId).build(),
                        hearingId)
                );
     }

    @Test
    public void shouldReturnCompanyRepresentativeUpdated(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(companyRepId, new CompanyRepresentative.Builder().withId(randomUUID()).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        final Stream<Object> objectStream = companyRepresentativeDelegate.updateCompanyRepresentative(
                new CompanyRepresentative.Builder().withId(companyRepId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final CompanyRepresentativeUpdated companyRepresentativeUpdated = (CompanyRepresentativeUpdated) event.get(0);
        assertThat(companyRepresentativeUpdated.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldReturnCompanyRepresentativeChangeIgnoredWhenCompanyRepresentativeMatch(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(companyRepId, new CompanyRepresentative.Builder().withId(companyRepId).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        final Stream<Object> objectStream = companyRepresentativeDelegate.updateCompanyRepresentative(
                new CompanyRepresentative.Builder().withId(companyRepId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final CompanyRepresentativeChangeIgnored companyRepresentativeChangeIgnored = (CompanyRepresentativeChangeIgnored) event.get(0);
        assertThat(companyRepresentativeChangeIgnored.getReason(),
                is("No change in provided company representative, payload ["+new CompanyRepresentative.Builder().withId(companyRepId).build()+"]"));
    }

    @Test
    public void shouldReturnCompanyRepresentativeChangeIgnoredWhenCompanyRepresentativeNotMatch(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(randomUUID(), new CompanyRepresentative.Builder().withId(randomUUID()).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        final Stream<Object> objectStream = companyRepresentativeDelegate.updateCompanyRepresentative(
                new CompanyRepresentative.Builder().withId(companyRepId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final CompanyRepresentativeChangeIgnored companyRepresentativeChangeIgnored = (CompanyRepresentativeChangeIgnored) event.get(0);
        assertThat(companyRepresentativeChangeIgnored.getReason(),
                is("Provided company representative does not exists, payload ["+new CompanyRepresentative.Builder().withId(companyRepId).build()+"]"));
    }

    @Test
    public void shouldHandleCompanyRepresentativeUpdated(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        companyRepresentativeDelegate.handleCompanyRepresentativeUpdated(
                new CompanyRepresentativeUpdated(new CompanyRepresentative.Builder().withId(companyRepId).build(),
                        hearingId)
        );
    }

    @Test
    public void shouldReturnCompanyRepresentativeChangeIgnoredWhenCompanyRepresentativeNotExists(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final UUID id = randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(companyRepId, new CompanyRepresentative.Builder().withId(companyRepId).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        final Stream<Object> objectStream = companyRepresentativeDelegate.removeCompanyRepresentative(
                  id
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final CompanyRepresentativeChangeIgnored companyRepresentativeChangeIgnored = (CompanyRepresentativeChangeIgnored) event.get(0);
        assertThat(companyRepresentativeChangeIgnored.getReason(),
                is("Provided company representative does not exists, payload ["+id.toString()+"]"));
    }

    @Test
    public void shouldReturnCompanyRepresentativeRemovedWhenCompanyRepresentativeExists(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final UUID id = randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(id, new CompanyRepresentative.Builder().withId(companyRepId).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        final Stream<Object> objectStream = companyRepresentativeDelegate.removeCompanyRepresentative(
                id
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final CompanyRepresentativeRemoved companyRepresentativeRemoved = (CompanyRepresentativeRemoved) event.get(0);
        assertThat(companyRepresentativeRemoved.getId(), is(id));
    }

    @Test
    public void shouldHandleCompanyRepresentativeRemoved(){
        final UUID hearingId = randomUUID();
        final UUID companyRepId = UUID.randomUUID();
        final UUID id = randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getCompanyRepresentatives().put(id, new CompanyRepresentative.Builder().withId(companyRepId).build());
        final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);
        companyRepresentativeDelegate.handleCompanyRepresentativeRemoved(new CompanyRepresentativeRemoved(id,hearingId));
     }
}
