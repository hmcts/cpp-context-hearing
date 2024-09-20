package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Address;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Interpreter;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Plea;
import uk.gov.moj.cpp.hearing.domain.event.CaseEjected;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeletedForProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CaseAggregateTest {


    @InjectMocks
    private CaseAggregate caseAggregate;

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(caseAggregate);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void testRecordSendingSheetComplete() throws Exception {
        final SendingSheetCompleted sendingSheetCompleted = createSendingSheet();

        Stream<Object> events = caseAggregate.recordSendingSheetComplete(sendingSheetCompleted);
        List<Object> lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(1, lEvents.size());
        Object event;

        event = lEvents.get(0);
        Assert.assertEquals(event.getClass(), SendingSheetCompletedRecorded.class);
        final SendingSheetCompletedRecorded typedEvent = (SendingSheetCompletedRecorded) event;
        Assert.assertEquals(typedEvent.getCrownCourtHearing(), sendingSheetCompleted.getCrownCourtHearing());
        Assert.assertEquals(typedEvent.getHearing(), sendingSheetCompleted.getHearing());

        //check that the second call does not result in another recording !
        events = caseAggregate.recordSendingSheetComplete(sendingSheetCompleted);
        lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(1, lEvents.size());
        event = lEvents.get(0);
        Assert.assertEquals(event.getClass(), SendingSheetCompletedPreviouslyRecorded.class);
        final SendingSheetCompletedPreviouslyRecorded typedNonEvent = (SendingSheetCompletedPreviouslyRecorded) event;
        Assert.assertEquals(typedNonEvent.getCrownCourtHearing(), typedNonEvent.getCrownCourtHearing());
        Assert.assertEquals(typedNonEvent.getHearing(), sendingSheetCompleted.getHearing());

    }


    private SendingSheetCompleted createSendingSheet() {

        final List<Defendant> arrDefendants = new ArrayList<>();
        final List lOffences = new ArrayList<>();
        final Plea plea = (new Plea.Builder()).withId(UUID.randomUUID()).withPleaValue("GUILTY").withPleaDate(LocalDate.now()).build();
        final LocalDate convictionDate = plea != null && plea.getValue().equals("GUILTY") ? plea.getPleaDate() : null;
        UUID offenceId = UUID.randomUUID();
        final Offence offence = (new Offence.Builder()).withId(offenceId).withCategory("category").withConvictionDate(convictionDate).
                withDescription("testOffence").withEndDate(LocalDate.now()).withPlea(plea)
                .withReason("Reason")
                .withSection("section")
                .withWording("wording")
                .withOffenceCode("offenceCode")
                .build();
        lOffences.add(offence);

        final Address address = Address.address().withAddress1("addr1").withAddress2("addr2").withAddress3("addr3").
                withAddress4("addr4").withPostcode("AA1 1AA").build();
        final UUID defendantId = UUID.randomUUID();
        final Interpreter interpreter = Interpreter.interpreter().withLanguage("English").withNeeded(true).build();
        final Defendant defendant = (new Defendant.Builder()).withOffences(lOffences).withAddress(address).withBailStatus("bailStatus").
                withDateOfBirth(LocalDate.now()).withDefenceOrganisation("CPP").
                withFirstName("Geoff").withLastName("ssdfsf").withGender("Male").withId(defendantId).
                withInterpreter(interpreter).withNationality("UK").withPersonId(UUID.randomUUID()).
                build();
        arrDefendants.add(defendant);

        final UUID caseId = UUID.randomUUID();

        final Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).withCaseUrn("caseUrn").
                withCourtCentreId("courtCentreId").withCourtCentreName("courtCentreName").
                withDefendants(arrDefendants).build();

        final UUID courtCentreId = UUID.randomUUID();
        final CrownCourtHearing crownCourtHearing = (new CrownCourtHearing.Builder()).withCcHearingDate("ccHearingDate").
                withCourtCentreId(courtCentreId).withCourtCentreName("courtCentrName").build();

        final SendingSheetCompleted sendingSheetCompleted = (new SendingSheetCompleted.Builder()).withHearing(hearing).
                withCrownCourtHearing(crownCourtHearing).build();
        return sendingSheetCompleted;
    }

    @Test
    public void shouldRaiseHearingDeletedForProsecutionCaseEvent() {

        final UUID hearingId = UUID.randomUUID();
        final UUID prosecutionCaseId = UUID.randomUUID();

        caseAggregate.apply(new HearingDeletedForProsecutionCase(prosecutionCaseId, hearingId));

        final List<Object> eventStream = caseAggregate.deleteHearingForProsecutionCase(prosecutionCaseId, hearingId).collect(toList());

        assertThat(eventStream.size(), is(1));
        final HearingDeletedForProsecutionCase hearingDeleted = (HearingDeletedForProsecutionCase) eventStream.get(0);
        assertThat(hearingDeleted.getHearingId(), is(hearingId));
        assertThat(hearingDeleted.getProsecutionCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldRaiseHearingUnallocatedForProsecutionCaseEvent() {

        final UUID hearingId = UUID.randomUUID();
        final UUID prosecutionCaseId = UUID.randomUUID();

        caseAggregate.apply(new HearingRemovedForProsecutionCase(prosecutionCaseId, hearingId));

        final List<Object> eventStream = caseAggregate.removeHearingForProsecutionCase(prosecutionCaseId, hearingId).collect(toList());

        assertThat(eventStream.size(), is(1));
        final HearingRemovedForProsecutionCase hearingDeleted = (HearingRemovedForProsecutionCase) eventStream.get(0);
        assertThat(hearingDeleted.getHearingId(), is(hearingId));
        assertThat(hearingDeleted.getProsecutionCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldRaiseCaseEjectedEventWhenHearingIdsPassedIsEmpty(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        ReflectionUtil.setField(caseAggregate, "hearingIds", Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        final List<Object> eventStream = caseAggregate.ejectCase(prosecutionCaseId, Collections.emptyList()).collect(toList());
        assertThat(eventStream.size(), is(1));
        final CaseEjected caseEjected = (CaseEjected) eventStream.get(0);
        assertThat(caseEjected.getProsecutionCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldReturnEmptyEventWhenHearingIdsIsEmpty(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        ReflectionUtil.setField(caseAggregate, "hearingIds", Collections.emptyList());
        final List<Object> eventStream = caseAggregate.caseDefendantsUpdated(new ProsecutionCase.Builder().withId(prosecutionCaseId).build()).collect(toList());
        assertThat(eventStream.size(), is(0));
    }

    @Test
    public void shouldReturnEmptyEventInEnrichUpdateCaseMarkersWhenHearingIdsIsEmpty(){
        final UUID prosecutionCaseId = UUID.randomUUID();
        ReflectionUtil.setField(caseAggregate, "hearingIds", Collections.emptyList());
        final List<Object> eventStream = caseAggregate.enrichUpdateCaseMarkersWithHearingIds(prosecutionCaseId, Collections.emptyList()).collect(toList());
        assertThat(eventStream.size(), is(0));
    }
}