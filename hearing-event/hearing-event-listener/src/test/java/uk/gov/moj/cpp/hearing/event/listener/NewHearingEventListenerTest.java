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
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.NewProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;

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

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Captor
    private ArgumentCaptor<Ahearing> hearingexArgumentCaptor;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    private JsonEnvelope getInitiateAhearingJsonEnvelope(List<Case> cases, uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing) {
        //TODO review unexpected use of InitiateHearingCommand that contains cases
        InitiateHearingCommand document = new InitiateHearingCommand(cases, hearing);
        ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        //objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

        String strJsonDocument;
        try {
            strJsonDocument = objectMapper.writer().writeValueAsString(document);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("failed ot serialise " + document, jpe);
        }
        JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();
        return new DefaultJsonEnvelope(null, jsonObject);
    }

    private JsonEnvelope getAddDefenceCounselJsonEnvelope(NewDefenceCounselAdded newDefenceCounselAdded) {
        ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

        String strJsonDocument;
        try {
            strJsonDocument = objectMapper.writer().writeValueAsString(newDefenceCounselAdded);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("failed to serialise " + newDefenceCounselAdded, jpe);
        }
        JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();
        return new DefaultJsonEnvelope(null, jsonObject);
    }

    private JsonEnvelope getAddProsecutionCounselJsonEnvelope(NewProsecutionCounselAdded newProsecutionCounselAdded) {
        ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

        String strJsonDocument;
        try {
            strJsonDocument = objectMapper.writer().writeValueAsString(newProsecutionCounselAdded);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("failed to serialise " + newProsecutionCounselAdded, jpe);
        }
        JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();
        return new DefaultJsonEnvelope(null, jsonObject);
    }


    @Test
    public void shouldStoreProsecutionCounselOnAddEvent() {
        NewProsecutionCounselAdded newProsecutionCounselAdded = NewProsecutionCounselAdded.builder()
                .withAttendeeId(randomUUID())
                .withFirstName("david")
                .withHearingId(randomUUID())
                .withStatus("QC")
                .withLastName("Bowie")
                .withTitle("Mr")
                .build();
        JsonEnvelope event = getAddProsecutionCounselJsonEnvelope(newProsecutionCounselAdded);

        Ahearing ahearing = Ahearing.builder().withId(newProsecutionCounselAdded.getHearingId()).build();
        when(this.ahearingRepository.findBy(newProsecutionCounselAdded.getHearingId())).thenReturn(ahearing);


        this.newHearingEventListener.newProsecutionCounselAdded(event);

        //check the hearing was saved and that it had the defence advocate
        verify(this.ahearingRepository).save(this.hearingexArgumentCaptor.capture());
        Assert.assertTrue(ahearing == this.hearingexArgumentCaptor.getValue());
        Assert.assertEquals(ahearing.getAttendees().size(), 1);
        Assert.assertTrue(ahearing.getAttendees().get(0) instanceof ProsecutionAdvocate);
        ProsecutionAdvocate prosecutionAdvocate = (ProsecutionAdvocate) ahearing.getAttendees().get(0);
        //check that the attendee has been added
        Assert.assertEquals(prosecutionAdvocate.getId().getId(), newProsecutionCounselAdded.getAttendeeId());
        Assert.assertEquals(prosecutionAdvocate.getId().getHearingId(), newProsecutionCounselAdded.getHearingId());
        Assert.assertEquals(prosecutionAdvocate.getStatus(), newProsecutionCounselAdded.getStatus());
        Assert.assertEquals(prosecutionAdvocate.getFirstName(), newProsecutionCounselAdded.getFirstName());
        Assert.assertEquals(prosecutionAdvocate.getLastName(), newProsecutionCounselAdded.getLastName());
        Assert.assertEquals(prosecutionAdvocate.getTitle(), newProsecutionCounselAdded.getTitle());

        //  now check an update works
        newProsecutionCounselAdded = NewProsecutionCounselAdded.builder()
                .withAttendeeId(newProsecutionCounselAdded.getAttendeeId())
                .withFirstName("Xdavid")
                .withHearingId(newProsecutionCounselAdded.getHearingId())
                .withStatus("Trainee")
                .withLastName("XBowie")
                .withTitle("XMr")
                .build();

        reset(this.ahearingRepository);
        when(this.ahearingRepository.findBy(newProsecutionCounselAdded.getHearingId())).thenReturn(ahearing);

        event = getAddProsecutionCounselJsonEnvelope(newProsecutionCounselAdded);

        this.newHearingEventListener.newProsecutionCounselAdded(event);

        //check the the status only was updated
        verify(this.ahearingRepository).save(this.hearingexArgumentCaptor.capture());
        Assert.assertTrue(ahearing == this.hearingexArgumentCaptor.getValue());
        Assert.assertEquals(ahearing.getAttendees().size(), 1);
        Assert.assertTrue(ahearing.getAttendees().get(0) instanceof ProsecutionAdvocate);
        prosecutionAdvocate = (ProsecutionAdvocate) ahearing.getAttendees().get(0);
        //check that the attendee has been added
        Assert.assertEquals(prosecutionAdvocate.getId().getId(), newProsecutionCounselAdded.getAttendeeId());
        Assert.assertEquals(prosecutionAdvocate.getId().getHearingId(), newProsecutionCounselAdded.getHearingId());
        Assert.assertEquals(prosecutionAdvocate.getStatus(), newProsecutionCounselAdded.getStatus());
        Assert.assertNotEquals(prosecutionAdvocate.getFirstName(), newProsecutionCounselAdded.getFirstName());
        Assert.assertNotEquals(prosecutionAdvocate.getLastName(), newProsecutionCounselAdded.getLastName());
        Assert.assertNotEquals(prosecutionAdvocate.getTitle(), newProsecutionCounselAdded.getTitle());

    }

    @Test
    public void shouldStoreDefenceCounselOnAddEvent() {
        UUID hearingId = randomUUID();
        UUID attendeeId = randomUUID();
        List<UUID> defendantIds = asList(randomUUID(), randomUUID());
        UUID personId = null;
        String status = "QC";
        NewDefenceCounselAdded newDefenceCounselAdded =
                NewDefenceCounselAdded.builder()
                        .withHearingId(hearingId)
                        .withAttendeeId(attendeeId)
                        .withPersonId(personId)
                        .withDefendantIds(defendantIds)
                        .withStatus(status)
                        .withFirstName("David")
                        .withLastName("Davidson")
                        .withTitle("Colonel")
                        .build();
        JsonEnvelope event = getAddDefenceCounselJsonEnvelope(newDefenceCounselAdded);
        List<Defendant> defendants = asList(
                Defendant.builder().withId(new HearingSnapshotKey(defendantIds.get(0), hearingId)).build(),
                Defendant.builder().withId(new HearingSnapshotKey(defendantIds.get(1), hearingId)).build());
        Ahearing ahearing = Ahearing.builder().withId(hearingId).withDefendants(defendants).build();
        when(this.ahearingRepository.findBy(hearingId)).thenReturn(ahearing);

        this.newHearingEventListener.newDefenceCounselAdded(event);

        //check the hearing was saved and that it had the defence advocate
        verify(this.ahearingRepository).save(this.hearingexArgumentCaptor.capture());
        //check that the defendants a
        Assert.assertTrue(ahearing == this.hearingexArgumentCaptor.getValue());
        Assert.assertEquals(ahearing.getAttendees().size(), 1);
        Assert.assertTrue(ahearing.getAttendees().get(0) instanceof DefenceAdvocate);
        DefenceAdvocate defenceAdvocate = (DefenceAdvocate) ahearing.getAttendees().get(0);
        //check that the attendee has been added
        Assert.assertEquals(defenceAdvocate.getId().getId(), attendeeId);
        Assert.assertEquals(defenceAdvocate.getId().getHearingId(), hearingId);
        Assert.assertEquals(defenceAdvocate.getStatus(), newDefenceCounselAdded.getStatus());

        Assert.assertEquals(defenceAdvocate.getFirstName(), newDefenceCounselAdded.getFirstName());
        Assert.assertEquals(defenceAdvocate.getLastName(), newDefenceCounselAdded.getLastName());
        Assert.assertEquals(defenceAdvocate.getTitle(), newDefenceCounselAdded.getTitle());
        //compare the defendants linked with store
        Set<UUID> linkedDefendantIds = defenceAdvocate.getDefendants().stream().map(d -> d.getId().getId()).collect(Collectors.toSet());
        Assert.assertEquals(linkedDefendantIds, new HashSet<>(newDefenceCounselAdded.getDefendantIds()));

        reset(this.ahearingRepository);
        when(this.ahearingRepository.findBy(hearingId)).thenReturn(ahearing);

        //check that an update only changes status
        newDefenceCounselAdded = NewDefenceCounselAdded.builder()
                .withStatus("changedStatus")
                .withTitle("changedTitle")
                .withFirstName("changedFirstName")
                .withLastName("changedLastName")
                .withAttendeeId(newDefenceCounselAdded.getAttendeeId())
                .withHearingId(newDefenceCounselAdded.getHearingId())
                .withDefendantIds(new ArrayList<>())
                .build();

        event = getAddDefenceCounselJsonEnvelope(newDefenceCounselAdded);
        this.newHearingEventListener.newDefenceCounselAdded(event);

        //check the hearing was saved and that it had the defence advocate
        verify(this.ahearingRepository).save(this.hearingexArgumentCaptor.capture());
        //check that the defendants a
        Assert.assertTrue(ahearing == this.hearingexArgumentCaptor.getValue());
        Assert.assertEquals(ahearing.getAttendees().size(), 1);
        Assert.assertTrue(ahearing.getAttendees().get(0) instanceof DefenceAdvocate);
        defenceAdvocate = (DefenceAdvocate) ahearing.getAttendees().get(0);
        //check that the attendee has been added
        Assert.assertEquals(defenceAdvocate.getId().getId(), attendeeId);
        Assert.assertEquals(defenceAdvocate.getId().getHearingId(), hearingId);
        Assert.assertEquals(defenceAdvocate.getStatus(), newDefenceCounselAdded.getStatus());

        //check other fields were not changed
        Assert.assertNotEquals(defenceAdvocate.getFirstName(), newDefenceCounselAdded.getFirstName());
        Assert.assertNotEquals(defenceAdvocate.getLastName(), newDefenceCounselAdded.getLastName());
        Assert.assertNotEquals(defenceAdvocate.getTitle(), newDefenceCounselAdded.getTitle());
        //compare the defendants linked with store
        linkedDefendantIds = defenceAdvocate.getDefendants().stream().map(d -> d.getId().getId()).collect(Collectors.toSet());
        Assert.assertNotEquals(linkedDefendantIds, new HashSet<>(newDefenceCounselAdded.getDefendantIds()));


    }


    @Test
    public void shouldInsertAhearingWhenInitiated() {

        InitiateHearingCommand command = initiateHearingCommandTemplate().build();

        Case legalCase = command.getCases().get(0);
        Hearing hearing = command.getHearing();
        uk.gov.moj.cpp.hearing.command.initiate.Defendant defendant = command.getHearing().getDefendants().get(0);
        uk.gov.moj.cpp.hearing.command.initiate.Offence offence = defendant.getOffences().get(0);

        when(this.ahearingRepository.findBy(command.getHearing().getId())).thenReturn(null);

        when(this.legalCaseRepository.findBy(legalCase.getCaseId()))
                .thenReturn(
                        LegalCase.builder()
                                .withId(legalCase.getCaseId())
                                .withCaseurn(legalCase.getUrn())
                                .build()
                );

        this.newHearingEventListener.newHearingInitiated(getInitiateAhearingJsonEnvelope(command.getCases(), command.getHearing()));

        ArgumentCaptor<Ahearing> hearingexArgumentCaptor = ArgumentCaptor.forClass(Ahearing.class);
        verify(this.ahearingRepository).save(hearingexArgumentCaptor.capture());
        final Ahearing actualHearing = hearingexArgumentCaptor.getValue();

        assertThat(actualHearing.getId(), is(hearing.getId()));
        assertThat(actualHearing.getCourtCentreId(), is(hearing.getCourtCentreId()));
        assertThat(actualHearing.getCourtCentreName(), is(hearing.getCourtCentreName()));
        assertThat(actualHearing.getRoomId(), is(hearing.getCourtRoomId()));
        assertThat(actualHearing.getRoomName(), is(hearing.getCourtRoomName()));
        assertThat(actualHearing.getStartDateTime().toLocalDateTime(), is(hearing.getStartDateTime().toLocalDateTime()));
        assertThat(actualHearing.getHearingType(), is(hearing.getType()));

        uk.gov.moj.cpp.hearing.persist.entity.ex.Judge actualJudge = actualHearing.getAttendees().stream()
                .filter(a -> a instanceof uk.gov.moj.cpp.hearing.persist.entity.ex.Judge)
                .map(uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.class::cast)
                .findFirst()
                .get();

        assertThat(actualJudge.getId().getId(), is(hearing.getJudge().getId()));
        assertThat(actualJudge.getId().getHearingId(), is(hearing.getId()));
        assertThat(actualJudge.getFirstName(), is(hearing.getJudge().getFirstName()));
        assertThat(actualJudge.getLastName(), is(hearing.getJudge().getLastName()));
        assertThat(actualJudge.getTitle(), is(hearing.getJudge().getTitle()));

        final Defendant actualDefendant = actualHearing.getDefendants().get(0);

        assertThat(actualDefendant.getId().getId(), is(defendant.getId()));
        assertThat(actualDefendant.getId().getHearingId(), is(hearing.getId()));
        assertThat(actualDefendant.getFirstName(), is(defendant.getFirstName()));
        assertThat(actualDefendant.getLastName(), is(defendant.getLastName()));
        assertThat(actualDefendant.getDateOfBirth(), is(defendant.getDateOfBirth()));
        assertThat(actualDefendant.getGender(), is(defendant.getGender()));
        assertThat(actualDefendant.getNationality(), is(defendant.getNationality()));
        assertThat(actualDefendant.getInterpreterLanguage(), is(defendant.getInterpreter().getLanguage()));
        assertThat(actualDefendant.getDefenceSolicitorFirm(), is(defendant.getDefenceOrganisation()));
        assertThat(actualDefendant.getDefendantCases().get(0).getBailStatus(), is(defendant.getDefendantCases().get(0).getBailStatus()));
        assertThat(actualDefendant.getDefendantCases().get(0).getCustodyTimeLimitDate().toLocalDateTime(), is(defendant.getDefendantCases().get(0).getCustodyTimeLimitDate().toLocalDateTime()));

        //assertThat(actualDefendant.getEmail(), is(""));
        //assertThat(actualDefendant.getFax(), is(""));
        //assertThat(actualDefendant.getHomeTelephone(), is(""));
        //assertThat(actualDefendant.getMobileTelephone(), is(""));
        //assertThat(actualDefendant.getWorkTelephone(), is(""));

        final uk.gov.moj.cpp.hearing.persist.entity.ex.Address actualAddress = actualDefendant.getAddress();

        assertThat(actualAddress.getAddress1(), is(defendant.getAddress().getAddress1()));
        assertThat(actualAddress.getAddress2(), is(defendant.getAddress().getAddress2()));
        assertThat(actualAddress.getAddress3(), is(defendant.getAddress().getAddress3()));
        assertThat(actualAddress.getAddress4(), is(defendant.getAddress().getAddress4()));
        assertThat(actualAddress.getPostCode(), is(defendant.getAddress().getPostCode()));

        final Offence actualOffence = actualDefendant.getOffences().get(0);

        assertThat(actualOffence.getId().getId(), is(offence.getId()));
        assertThat(actualOffence.getId().getHearingId(), is(hearing.getId()));
        assertThat(actualOffence.getDefendantId(), is(defendant.getId()));
        assertThat(actualOffence.getLegalCase().getId(), is(legalCase.getCaseId()));
        assertThat(actualOffence.getLegalCase().getCaseurn(), is(legalCase.getUrn()));
        assertThat(actualOffence.getLegislation(), is(offence.getLegislation()));
        assertThat(actualOffence.getTitle(), is(offence.getTitle()));
        assertThat(actualOffence.getWording(), is(offence.getWording()));
        assertThat(actualOffence.getEndDate(), is(offence.getEndDate()));
        assertThat(actualOffence.getStartDate(), is(offence.getStartDate()));
        assertThat(actualOffence.getCode(), is(offence.getOffenceCode()));

        //actualOffence.getVerdictId()
        //actualOffence.getPleaId()
        assertThat(actualOffence.getPleaDate(), nullValue());
        assertThat(actualOffence.getPleaValue(), nullValue());
        assertThat(actualOffence.getNumberOfJurors(), nullValue());
        assertThat(actualOffence.getNumberOfSplitJurors(), nullValue());
        assertThat(actualOffence.getUnanimous(), nullValue());
        assertThat(actualOffence.getVerdictCategory(), nullValue());
        assertThat(actualOffence.getVerdictCode(), nullValue());
        assertThat(actualOffence.getVerdictDate(), nullValue());
        assertThat(actualOffence.getVerdictDescription(), nullValue());

        ArgumentCaptor<LegalCase> caseArgumentCaptor = ArgumentCaptor.forClass(LegalCase.class);
        verify(this.legalCaseRepository).saveAndFlush(caseArgumentCaptor.capture());
        final LegalCase actualLegalCase = caseArgumentCaptor.getValue();

        assertThat(actualLegalCase.getId(), is(legalCase.getCaseId()));
        assertThat(actualLegalCase.getCaseurn(), is(legalCase.getUrn()));
    }


    @Test
    public void verdictUpdate_shouldUpdateTheVerdict() throws Exception {

        UUID hearingId = randomUUID();

        OffenceVerdictUpdated offenceVerdictUpdated = new OffenceVerdictUpdated(randomUUID(), hearingId, randomUUID(),
                randomUUID(), randomUUID(), STRING.next(), STRING.next(), STRING.next(), INTEGER.next(), INTEGER.next(),
                BOOLEAN.next(), PAST_LOCAL_DATE.next());

        Ahearing ahearing = Ahearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(new HearingSnapshotKey(offenceVerdictUpdated.getOffenceId(), hearingId))
                                                .build()
                                ))
                                .build()))
                .build();

        when(this.ahearingRepository.findById(hearingId)).thenReturn(ahearing);

        newHearingEventListener.verdictUpdate(envelopeFrom(metadataWithRandomUUID("hearing.offence-verdict-updated"),
                objectToJsonObjectConverter.convert(offenceVerdictUpdated)));

        verify(this.ahearingRepository).save(ahearing);

        Offence offence = ahearing.getDefendants().get(0).getOffences().get(0);

        assertThat(offence.getId().getId(), is(offenceVerdictUpdated.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offenceVerdictUpdated.getHearingId()));
        assertThat(offence.getVerdictId(), is(offenceVerdictUpdated.getVerdictId()));
        assertThat(offence.getVerdictCategory(), is(offenceVerdictUpdated.getCategory()));
        assertThat(offence.getVerdictCode(), is(offenceVerdictUpdated.getCode()));
        assertThat(offence.getVerdictDescription(), is(offenceVerdictUpdated.getDescription()));
        assertThat(offence.getNumberOfJurors(), is(offenceVerdictUpdated.getNumberOfJurors()));
        assertThat(offence.getNumberOfSplitJurors(), is(offenceVerdictUpdated.getNumberOfSplitJurors()));
        assertThat(offence.getUnanimous(), is(offenceVerdictUpdated.getUnanimous()));
        assertThat(offence.getVerdictDate(), is(offenceVerdictUpdated.getVerdictDate()));
    }

    @Test
    public void convictionDateUpdated_shouldUpdateTheConvictionDate() throws Exception {

        UUID hearingId = randomUUID();

        ConvictionDateAdded convictionDateAdded = new ConvictionDateAdded(randomUUID(), hearingId, randomUUID(),
                randomUUID(), PAST_LOCAL_DATE.next());

        Ahearing ahearing = Ahearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(new HearingSnapshotKey(convictionDateAdded.getOffenceId(), hearingId))
                                                .build()
                                ))
                                .build()))
                .build();

        when(this.ahearingRepository.findById(hearingId)).thenReturn(ahearing);

        newHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded)));

        verify(this.ahearingRepository).save(ahearing);

        Offence offence = ahearing.getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getId().getId(), is(convictionDateAdded.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(convictionDateAdded.getHearingId()));
        assertThat(offence.getConvictionDate(), is(convictionDateAdded.getConvictionDate()));
    }


    @Test
    public void convictionDateRemoved_shouldSetConvictionDateToNull() throws Exception {

        ConvictionDateRemoved convictionDateRemoved = new ConvictionDateRemoved(randomUUID(), randomUUID(), randomUUID(),
                randomUUID());

        Ahearing ahearing = Ahearing.builder().withId(convictionDateRemoved.getHearingId())
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(new HearingSnapshotKey(convictionDateRemoved.getOffenceId(), convictionDateRemoved.getHearingId()))
                                                .withConvictionDate(LocalDate.now())
                                                .build()
                                ))
                                .build()))
                .build();

        when(this.ahearingRepository.findById(convictionDateRemoved.getHearingId())).thenReturn(ahearing);

        newHearingEventListener.convictionDateRemoved(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved)));

        verify(this.ahearingRepository).save(ahearing);

        Offence offence = ahearing.getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getId().getId(), is(convictionDateRemoved.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(convictionDateRemoved.getHearingId()));
        assertThat(offence.getConvictionDate(), is(nullValue()));
    }

}
