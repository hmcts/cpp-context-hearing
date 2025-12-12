package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class ApplicantCounselDelegateTest {

    @Test
    public void shouldHandleApplicantCounselAdded(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        applicantCounselDelegate.handleApplicantCounselAdded(new ApplicantCounselAdded(
                new ApplicantCounsel.Builder().withId(applicantCounselId).build()
                ,hearingId
        ));
    }

    @Test
    public void shouldHandleApplicantCounselRemoved(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getApplicantCounsels().put(applicantCounselId,new ApplicantCounsel.Builder().withId(randomUUID()).build());
        momento.getApplicantCounsels().put(randomUUID(),new ApplicantCounsel.Builder().withId(randomUUID()).build());
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        applicantCounselDelegate.handleApplicantCounselRemoved(new ApplicantCounselRemoved(
                applicantCounselId
                ,hearingId
        ));
    }

    @Test
    public void shouldHandleApplicantCounselUpdated(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        applicantCounselDelegate.handleApplicantCounselUpdated(new ApplicantCounselUpdated(
                new ApplicantCounsel.Builder().withId(applicantCounselId).build()
                ,hearingId
        ));
    }

    @Test
    public void shouldAddApplicantCounsel(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        final Stream<Object> objectStream = applicantCounselDelegate.addApplicantCounsel(new ApplicantCounsel.Builder().withId(applicantCounselId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final ApplicantCounselAdded applicantCounselAdded = (ApplicantCounselAdded) event.get(0);
        assertThat(applicantCounselAdded.getHearingId(), is(hearingId));
        assertThat(applicantCounselAdded.getApplicantCounsel().getId(), is(applicantCounselId));
    }

    @Test
    public void shouldReturnApplicantCounselChangeIgnoredWhenCounselAlreadyExists(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getApplicantCounsels().put(applicantCounselId,new ApplicantCounsel.Builder().withId(randomUUID()).build());
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        final Stream<Object> objectStream = applicantCounselDelegate.addApplicantCounsel(
                new ApplicantCounsel.Builder().withId(applicantCounselId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final ApplicantCounselChangeIgnored applicantCounselChangeIgnored = (ApplicantCounselChangeIgnored) event.get(0);
        assertThat(applicantCounselChangeIgnored.getReason(),
                is("Provided applicantCounsel already exists, payload ["+new ApplicantCounsel.Builder().withId(applicantCounselId).build()+"]"));
    }

    @Test
    public void shouldRemoveApplicantCounsel(){
        final UUID hearingId = randomUUID();
        final UUID id = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        final Stream<Object> objectStream = applicantCounselDelegate.removeApplicantCounsel(
              id
            , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final ApplicantCounselRemoved applicantCounselRemoved = (ApplicantCounselRemoved) event.get(0);
        assertThat(applicantCounselRemoved.getId(), is(id));
        assertThat(applicantCounselRemoved.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldReturnApplicantCounselUpdated(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getApplicantCounsels().put(applicantCounselId,new ApplicantCounsel.Builder().withId(randomUUID()).build());
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        final Stream<Object> objectStream = applicantCounselDelegate.updateApplicantCounsel(
                new ApplicantCounsel.Builder().withId(applicantCounselId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final ApplicantCounselUpdated applicantCounselUpdated = (ApplicantCounselUpdated) event.get(0);
        assertThat(applicantCounselUpdated.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldReturnApplicantCounselChangeIgnoredWhenApplicationCounselMatch(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getApplicantCounsels().put(applicantCounselId,new ApplicantCounsel.Builder().withId(applicantCounselId).build());
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        final Stream<Object> objectStream = applicantCounselDelegate.updateApplicantCounsel(
                new ApplicantCounsel.Builder().withId(applicantCounselId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final ApplicantCounselChangeIgnored applicantCounselChangeIgnored = (ApplicantCounselChangeIgnored) event.get(0);
        assertThat(applicantCounselChangeIgnored.getReason(),
                is("No change in provided applicantCounsel, payload ["+new ApplicantCounsel.Builder().withId(applicantCounselId).build()+"]"));
    }

    @Test
    public void shouldReturnApplicantCounselChangeIgnoredWhenApplicationCounselNotMatch(){
        final UUID hearingId = randomUUID();
        final UUID applicantCounselId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.getApplicantCounsels().put(randomUUID(),new ApplicantCounsel.Builder().withId(randomUUID()).build());
        final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);
        final Stream<Object> objectStream = applicantCounselDelegate.updateApplicantCounsel(
                new ApplicantCounsel.Builder().withId(applicantCounselId).build()
                , hearingId);
        List<Object> event = objectStream.collect(Collectors.toList());
        final ApplicantCounselChangeIgnored applicantCounselChangeIgnored = (ApplicantCounselChangeIgnored) event.get(0);
        assertThat(applicantCounselChangeIgnored.getReason(),
                is("Provided applicantCounsel does not exists, payload ["+new ApplicantCounsel.Builder().withId(applicantCounselId).build()+"]"));
    }
}
