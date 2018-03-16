package uk.gov.moj.cpp.hearing.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Attendee;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class NewHearingEventListenerTest {


    @Mock
    private AhearingRepository ahearingRepository;

    @Mock
    private LegalCaseRepository legalCaseRepository;

    @InjectMocks
    private NewHearingEventListener newHearingEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Captor
    private ArgumentCaptor<Ahearing> hearingexArgumentCaptor;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper",
                new ObjectMapperProducer().objectMapper());
    }


    private JsonEnvelope getInitiateHearingexJsonEnvelope(List<Case> cases, uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing) {
        //TODO review unexpected use of InitiateHearingCommand that contains cases
        InitiateHearingCommand document = new InitiateHearingCommand(cases, hearing);
        ObjectMapper objectMapper =  new ObjectMapperProducer().objectMapper();
        //objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

        String strJsonDocument;
        try {
            strJsonDocument=objectMapper.writer().writeValueAsString(document);
        }
        catch (JsonProcessingException jpe) {
            throw new RuntimeException("failed ot serialise " + document, jpe);
        }
        JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();
        return new DefaultJsonEnvelope(null, jsonObject);
    }

    @Test
    public void shouldUpdateAhearingExWhenInitiated() {

        UUID caseId = UUID.randomUUID();

        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearingIn = new uk.gov.moj.cpp.hearing.command.initiate.Hearing.Builder()
                .withCourtCentreId(UUID.randomUUID())
                .withCourtCentreName("Liverpool2")
                .withCourtRoomId(UUID.randomUUID())
                .addDefendant(uk.gov.moj.cpp.hearing.command.initiate.Defendant.builder().withId(UUID.randomUUID())
                        .withAddress(Address.builder()
                                .withAddress1("adddr1")
                                .withAddress2("addr2")
                                .withAddress3("addr3")
                                .withAddress4("addr4")
                                .withPostCode("AA11AA")
                        )
                        .addOffence(uk.gov.moj.cpp.hearing.command.initiate.Offence.builder().withCaseId(caseId).withId(UUID.randomUUID())))
                .withJudge( Judge.builder().withFirstName("David").withLastName("Bowie").withId(UUID.randomUUID()))
                .build();
        final uk.gov.moj.cpp.hearing.command.initiate.Defendant defendantIn = hearingIn.getDefendants().get(0);

        List<Case> cases = Arrays.asList(Case.builder().withUrn("urn_xzy").withCaseId(caseId).build());
        final JsonEnvelope event = getInitiateHearingexJsonEnvelope(cases, hearingIn);
        //TODO determine equired behaviour if hearing already exists
        Ahearing hearingex = null;//new Ahearing.Builder().build();
        when(this.ahearingRepository.findBy(hearingIn.getId())).thenReturn(null);
        LegalCase legalCase = new LegalCase.Builder().withCaseurn("urn_xxx").withId(caseId).build();
        when(this.legalCaseRepository.findBy(caseId)).thenReturn(legalCase);

        //when(this.ahearingRepository.findBy(hearingIn.getId())).thenReturn(hearingex);

        this.newHearingEventListener.newHearingInitiated(event);
        verify(this.ahearingRepository).save(this.hearingexArgumentCaptor.capture());
        final Ahearing actualHearing = this.hearingexArgumentCaptor.getValue();
        assertThat(actualHearing.getId(), is(hearingIn.getId()));
        assertThat(actualHearing.getCourtCentreId(), is(hearingIn.getCourtCentreId()));
        assertThat(actualHearing.getCourtCentreName(), is(hearingIn.getCourtCentreName()));
        assertThat(actualHearing.getCourtCentreId(), is(hearingIn.getCourtCentreId()));
        assertThat(actualHearing.getDefendants().size(), is(1));
        assertThat(actualHearing.getAttendees().size(), is(2));

        final Predicate<Attendee> isJudge = a->a instanceof uk.gov.moj.cpp.hearing.persist.entity.ex.Judge;
        final long judgeCount = actualHearing.getAttendees().stream().filter(isJudge).count();
        Assert.assertEquals(judgeCount, 1);
        uk.gov.moj.cpp.hearing.persist.entity.ex.Judge judgeOut = (uk.gov.moj.cpp.hearing.persist.entity.ex.Judge) actualHearing.getAttendees().stream().filter(isJudge).findFirst().get();
        assertThat(judgeOut.getFirstName(), is(hearingIn.getJudge().getFirstName()));
        //TODO this is 2 because we fake a defence counsel !
        //TODO clear this mess up - WHEN the command stops inserting dummy advocates !
        Predicate<Attendee> isDefenceCounsel = a->a instanceof DefenceAdvocate;
        final long defenceAdvocateCount = actualHearing.getAttendees().stream().filter(isJudge).count();
        Assert.assertEquals(1, defenceAdvocateCount);
        final DefenceAdvocate defenceAdvocate = (DefenceAdvocate) actualHearing.getAttendees().stream().filter(isDefenceCounsel).findFirst().get();

        assertThat(judgeOut.getFirstName(), is(hearingIn.getJudge().getFirstName()));
        assertThat(judgeOut.getId().getId(), is(hearingIn.getJudge().getId()));
        assertThat(judgeOut.getId().getHearingId(), is(hearingIn.getId()));

        Assert.assertEquals(actualHearing.getDefendants().size(), hearingIn.getDefendants().size());
        final Defendant defendant = actualHearing.getDefendants().iterator().next();

        //TODO assert caseId is valid
        assertThat(defendant.getOffences().size(), is(hearingIn.getDefendants().get(0).getOffences().size()));
        Offence offence = defendant.getOffences().iterator().next();
        assertThat(offence.getDefendant().getId().getId(), is(defendantIn.getId()));
        assertThat(offence.getDefendantId(), is(defendant.getId().getId()));
        assertThat("offence case mandatory", offence.getLegalCase()!=null);
        assertThat( offence.getLegalCase().getId(), is(caseId));
        assertThat( offence.getLegalCase().getCaseurn(), is(legalCase.getCaseurn()));

        Address addressIn = defendantIn.getAddress();
        uk.gov.moj.cpp.hearing.persist.entity.ex.Address address =  defendant.getAddress();
        assertThat(address.getAddress1(), is(addressIn.getAddress1()));
        assertThat(address.getAddress2(), is(addressIn.getAddress2()));
        assertThat(address.getAddress3(), is(addressIn.getAddress3()));
        assertThat(address.getAddress4(), is(addressIn.getAddress4()));
        assertThat(address.getPostCode(), is(addressIn.getPostCode()));


    }


}
