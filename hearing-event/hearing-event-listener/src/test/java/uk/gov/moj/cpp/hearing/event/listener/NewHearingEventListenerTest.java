package uk.gov.moj.cpp.hearing.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
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
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Attendee;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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


    private JsonEnvelope getInitiateAhearingJsonEnvelope(List<Case> cases, uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing) {
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

    private JsonEnvelope getAddDefenceCounselJsonEnvelope(NewDefenceCounselAdded newDefenceCounselAdded) {
        ObjectMapper objectMapper =  new ObjectMapperProducer().objectMapper();

        String strJsonDocument;
        try {
            strJsonDocument=objectMapper.writer().writeValueAsString(newDefenceCounselAdded);
        }
        catch (JsonProcessingException jpe) {
            throw new RuntimeException("failed to serialise " + newDefenceCounselAdded, jpe);
        }
        JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();
        return new DefaultJsonEnvelope(null, jsonObject);
    }


    @Test
    public void shouldStoreDefenceCounselOnAddEvent() {
        UUID hearingId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();
        List<UUID> defendantIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        UUID personId = null;
        String status = "QC";
        NewDefenceCounselAdded newDefenceCounselAdded = new NewDefenceCounselAdded(hearingId, attendeeId, personId, defendantIds, status);
        final JsonEnvelope event = getAddDefenceCounselJsonEnvelope(newDefenceCounselAdded);
        List<Defendant> defendants= Arrays.asList(
                Defendant.builder().withId(new HearingSnapshotKey(defendantIds.get(0), hearingId)).build(),
                Defendant.builder().withId(new HearingSnapshotKey(defendantIds.get(1), hearingId)).build() );
        Ahearing ahearing = Ahearing.builder().withId(hearingId).withDefendants( defendants).build();
        when(this.ahearingRepository.findBy(hearingId)).thenReturn(ahearing);


        this.newHearingEventListener.newDefenceCounselAdded(event);

        //check the hearing was saved and that it had the defence advocate
        verify(this.ahearingRepository).save(this.hearingexArgumentCaptor.capture());
        //check that the defendants a
        Assert.assertTrue(ahearing==this.hearingexArgumentCaptor.getValue());
        Assert.assertEquals(ahearing.getAttendees().size(), 1);
        Assert.assertTrue(ahearing.getAttendees().get(0) instanceof DefenceAdvocate);
        DefenceAdvocate defenceAdvocate = (DefenceAdvocate) ahearing.getAttendees().get(0);
        //check that the attendee has been added
        Assert.assertEquals(defenceAdvocate.getId().getId(), attendeeId);
        Assert.assertEquals(defenceAdvocate.getId().getHearingId(), hearingId);
        Assert.assertEquals(defenceAdvocate.getStatus(), newDefenceCounselAdded.getStatus());
        //compare the defendants linked with store
        Set<UUID> linkedDefendantIds = defenceAdvocate.getDefendants().stream().map(d->d.getId().getId()).collect(Collectors.toSet());
        Assert.assertEquals(linkedDefendantIds, new HashSet<>(newDefenceCounselAdded.getDefendantIds()));

        //TODO add the other fields of defenceAdvocate (add to command also!)

    }

    @Test
    public void shouldInsertAhearingWhenInitiated() {

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
        final JsonEnvelope event = getInitiateAhearingJsonEnvelope(cases, hearingIn);
        when(this.ahearingRepository.findBy(hearingIn.getId())).thenReturn(null);
        LegalCase legalCase = LegalCase.builder().withCaseurn("urn_xzy").withId(caseId).build();
        when(this.legalCaseRepository.findBy(caseId)).thenReturn(legalCase);
        this.newHearingEventListener.newHearingInitiated(event);
        verify(this.ahearingRepository).save(this.hearingexArgumentCaptor.capture());
        final Ahearing actualHearing = this.hearingexArgumentCaptor.getValue();
        assertThat(actualHearing.getId(), is(hearingIn.getId()));
        assertThat(actualHearing.getCourtCentreId(), is(hearingIn.getCourtCentreId()));
        assertThat(actualHearing.getCourtCentreName(), is(hearingIn.getCourtCentreName()));
        assertThat(actualHearing.getCourtCentreId(), is(hearingIn.getCourtCentreId()));
        assertThat(actualHearing.getDefendants().size(), is(1));

        final Predicate<Attendee> isJudge = a->a instanceof uk.gov.moj.cpp.hearing.persist.entity.ex.Judge;
        final long judgeCount = actualHearing.getAttendees().stream().filter(isJudge).count();
        Assert.assertEquals(judgeCount, 1);
        uk.gov.moj.cpp.hearing.persist.entity.ex.Judge judgeOut = (uk.gov.moj.cpp.hearing.persist.entity.ex.Judge) actualHearing.getAttendees().stream().filter(isJudge).findFirst().get();
        assertThat(judgeOut.getFirstName(), is(hearingIn.getJudge().getFirstName()));
        final long defenceAdvocateCount = actualHearing.getAttendees().stream().filter(isJudge).count();
        Assert.assertEquals(1, defenceAdvocateCount);

        assertThat(judgeOut.getFirstName(), is(hearingIn.getJudge().getFirstName()));
        assertThat(judgeOut.getId().getId(), is(hearingIn.getJudge().getId()));
        assertThat(judgeOut.getId().getHearingId(), is(hearingIn.getId()));

        Assert.assertEquals(actualHearing.getDefendants().size(), hearingIn.getDefendants().size());
        final Defendant defendant = actualHearing.getDefendants().iterator().next();
        assertThat(defendant.getFirstName(), is(defendantIn.getFirstName()));
        assertThat(defendant.getLastName(), is(defendantIn.getLastName()));
        assertThat(defendant.getDateOfBirth(), is(defendantIn.getDateOfBirth()));
        //        assertThat(defendant.getEmail(), is(defendantIn.get()));

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

        //TODO assert that cases were saved to LegalCase repository


    }


}
