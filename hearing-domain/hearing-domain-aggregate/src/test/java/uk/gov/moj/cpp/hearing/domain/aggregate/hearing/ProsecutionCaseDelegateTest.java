package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class ProsecutionCaseDelegateTest {

    @Test
    public void shouldHandleCaseMarkersUpdated(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingAggregateMomento hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(UUID.randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(hearingAggregateMomento);
        prosecutionCaseDelegate.handleCaseMarkersUpdated(new CaseMarkersUpdated(
                prosecutionCaseId,
                hearingId,
                Arrays.asList(new Marker.Builder().withId(UUID.randomUUID())
                        .withMarkerTypeCode("cd1")
                        .withMarkerTypeid(UUID.randomUUID())
                        .withMarkerTypeDescription("description1")
                        .build())
        ));
    }

    @Test
    public void shouldHandleProsecutorUpdated(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingAggregateMomento hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(UUID.randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(hearingAggregateMomento);
        prosecutionCaseDelegate.handleProsecutorUpdated(new CpsProsecutorUpdated(
                hearingId,
                prosecutionCaseId,
                UUID.randomUUID(),
                "",
                "",
                "",
                "",
                Address.address().build()));
    }

    @Test
    public void shouldReturnStreamWhenMomentoIsNotPublished(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingAggregateMomento hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setPublished(false);
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(UUID.randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(hearingAggregateMomento);
        final Stream<Object> objectStream = prosecutionCaseDelegate.updateCaseMarkers(
                hearingId,
                prosecutionCaseId,
                null);
        final CaseMarkersUpdated caseMarkersUpdated = (CaseMarkersUpdated) objectStream.collect(Collectors.toList()).get(0);
        assertThat(caseMarkersUpdated.getProsecutionCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldReturnEmptyStreamWhenMomentoIsPublished(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingAggregateMomento hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setPublished(true);
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(UUID.randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(hearingAggregateMomento);
        final Stream<Object> objectStream = prosecutionCaseDelegate.updateCaseMarkers(
                hearingId,
                prosecutionCaseId,
                null);
        assertThat(objectStream.collect(Collectors.toList()).size(), is(0));
    }

    @Test
    public void shouldReturnStreamOfCpsProsecutorUpdatedWhenMomentoIsNotPublished(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingAggregateMomento hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setPublished(false);
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(UUID.randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(hearingAggregateMomento);
        final Stream<Object> objectStream = prosecutionCaseDelegate.updateProsecutor(
                hearingId,
                prosecutionCaseId,
                new ProsecutionCaseIdentifier.Builder().withProsecutionAuthorityId(UUID.randomUUID())
                        .withProsecutionAuthorityCode("cd")
                        .withProsecutionAuthorityName("name1")
                        .withProsecutionAuthorityReference("ref1")
                        .withCaseURN("urn1")
                        .withAddress(null)
                        .build());
        final CpsProsecutorUpdated cpsProsecutorUpdated = (CpsProsecutorUpdated) objectStream.collect(Collectors.toList()).get(0);
        assertThat(cpsProsecutorUpdated.getProsecutionCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldReturnEmptyStreamOfCpsWhenMomentoIsPublished(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingAggregateMomento hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setPublished(true);
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(UUID.randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(hearingAggregateMomento);
        final Stream<Object> objectStream = prosecutionCaseDelegate.updateProsecutor(
                hearingId,
                prosecutionCaseId,
                new ProsecutionCaseIdentifier.Builder().withProsecutionAuthorityId(UUID.randomUUID())
                        .withProsecutionAuthorityCode("cd")
                        .withProsecutionAuthorityName("name1")
                        .withProsecutionAuthorityReference("ref1")
                        .withCaseURN("urn1")
                        .withAddress(null)
                        .build());
        assertThat(objectStream.collect(Collectors.toList()).size(), is(0));
    }

    @Test
    public void shouldSetLegalAidStatusOnDefendantForHearing(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingAggregateMomento hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setPublished(true);
        final UUID defendantId = UUID.randomUUID();
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                        .withId(defendantId)
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(hearingAggregateMomento);
        prosecutionCaseDelegate.onDefendantLegalaidStatusTobeUpdatedForHearing(
                new DefendantLegalAidStatusUpdatedForHearing.Builder()
                        .withDefendantId(defendantId)
                        .withHearingId(hearingId)
                        .withLegalAidStatus("legalAid")
                        .build());
    }
}
