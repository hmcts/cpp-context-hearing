package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

@RunWith(MockitoJUnitRunner.class)
public class NewHearingEventListenerTest {

    @Mock
    private AhearingRepository ahearingRepository;

    @Mock
    private LegalCaseRepository legalCaseRepository;
    
    @Mock
    private OffenceRepository offenceRepository;

    @InjectMocks
    private NewHearingEventListener newHearingEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

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
        assertThat(actualOffence.getLegalCase().getCaseUrn(), is(legalCase.getUrn()));
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
        assertThat(actualLegalCase.getCaseUrn(), is(legalCase.getUrn()));
    }

    @Test
    public void convictionDateUpdated_shouldUpdateTheConvictionDate() throws Exception {

        final UUID offenceId = randomUUID();
        final UUID hearingId = offenceId;
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        final ConvictionDateAdded convictionDateAdded = new ConvictionDateAdded(hearingId, offenceId, PAST_LOCAL_DATE.next());

        final Ahearing ahearing = Ahearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(snapshotKey)
                                                .build()
                                ))
                                .build()))
                .build();

        final Offence offence = ahearing.getDefendants().get(0).getOffences().get(0);

        when(this.offenceRepository.findBySnapshotKey(snapshotKey)).thenReturn(offence);

        newHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(convictionDateAdded.getOffenceId()));
        assertThat(offence.getConvictionDate(), is(convictionDateAdded.getConvictionDate()));
    }


    @Test
    public void convictionDateRemoved_shouldSetConvictionDateToNull() throws Exception {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        final ConvictionDateRemoved convictionDateRemoved = new ConvictionDateRemoved(hearingId, offenceId);
        final Ahearing ahearing = Ahearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(snapshotKey)
                                                .withConvictionDate(LocalDate.now())
                                                .build()
                                ))
                                .build()))

                .build();
        
        final Offence offence = ahearing.getDefendants().get(0).getOffences().get(0);
        
        when(offenceRepository.findBySnapshotKey(snapshotKey)).thenReturn(offence);

        newHearingEventListener.convictionDateRemoved(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(offenceId));
        assertThat(offence.getId().getHearingId(), is(hearingId));
        assertThat(offence.getConvictionDate(), is(nullValue()));
    }
    
    
    @Test
    public void testHearingInitiatedPleaData() throws Exception {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID originHearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final LocalDate pleaDate = LocalDate.now();
        final String pleaValue = "GUILTY";
        final UUID caseId = randomUUID();
        
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        
        final Ahearing ahearing = Ahearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withId(new HearingSnapshotKey(defendantId, hearingId))
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(snapshotKey)
                                                .withCase(LegalCase.builder()
                                                        .withId(caseId)
                                                        .withCaseurn("TEST")
                                                        .build())
                                                .withPleaDate(pleaDate)
                                                .withPleaValue(pleaValue)
                                                .build()
                                ))
                                .build()))

                .build();
        
        final InitiateHearingOffencePlead event = InitiateHearingOffencePlead.builder()
                .withOffenceId(offenceId)
                .withCaseId(caseId)
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withOriginHearingId(originHearingId)
                .withPleaDate(pleaDate)
                .withValue(pleaValue)
                .build();
        
        final Offence offence = ahearing.getDefendants().get(0).getOffences().get(0);
        
        when(offenceRepository.findBySnapshotKey(snapshotKey)).thenReturn(offence);

        newHearingEventListener.hearingInitiatedPleaData(envelopeFrom(metadataWithRandomUUID("hearing.initiate-hearing-offence-plead"),
                objectToJsonObjectConverter.convert(event)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(offenceId));
        assertThat(offence.getId().getHearingId(), is(hearingId));
        assertThat(offence.getOriginHearingId(), is(originHearingId));
        assertThat(offence.getPleaDate(), is(pleaDate));
        assertThat(offence.getPleaValue(), is(pleaValue));
    }

}
