package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
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
import org.mockito.Captor;
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
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Judge;
import uk.gov.moj.cpp.hearing.repository.WitnessRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

@RunWith(MockitoJUnitRunner.class)
public class InitiateHearingEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private WitnessRepository witnessRepository;


    @Mock
    private LegalCaseRepository legalCaseRepository;
    
    @Mock
    private OffenceRepository offenceRepository;

    @InjectMocks
    private InitiateHearingEventListener initiateHearingEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Captor
    private ArgumentCaptor<Witness> witnessArgumentCaptor;

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_PERSON_ID = "personId";
    public static final String FIELD_CLASSIFICATION = "classification";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";
    public static final String FIELD_DEFENDANT_IDS = "defendantIds";
    public static final String FIELD_DEFENDANT_ID = "defendantId";
    public static final String WITNESS_TYPE = "defence";
    public static final String WITNESS_CLASSIFICATION = "expert";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID PERSON_ID = randomUUID();
    private static final UUID TARGET_ID = randomUUID();

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    private JsonEnvelope getInitiateAhearingJsonEnvelope(final List<Case> cases, final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing) {
        final InitiateHearingCommand document = new InitiateHearingCommand(cases, hearing);
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

        String strJsonDocument;
        try {
            strJsonDocument = objectMapper.writer().writeValueAsString(document);
        } catch (final JsonProcessingException jpe) {
            throw new RuntimeException("failed ot serialise " + document, jpe);
        }
        final JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();
        return new DefaultJsonEnvelope(null, jsonObject);
    }

    @Test
    public void shouldInsertAhearingWhenInitiated() {

        final InitiateHearingCommand command = initiateHearingCommandTemplate().build();

        final Case legalCase = command.getCases().get(0);
        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing = command.getHearing();
        final uk.gov.moj.cpp.hearing.command.initiate.Defendant defendant = command.getHearing().getDefendants().get(0);
        final uk.gov.moj.cpp.hearing.command.initiate.Offence offence = defendant.getOffences().get(0);

        when(this.hearingRepository.findBy(command.getHearing().getId())).thenReturn(null);

        when(this.legalCaseRepository.findBy(legalCase.getCaseId()))
                .thenReturn(
                        LegalCase.builder()
                                .withId(legalCase.getCaseId())
                                .withCaseurn(legalCase.getUrn())
                                .build()
                );

        this.initiateHearingEventListener.newHearingInitiated(getInitiateAhearingJsonEnvelope(command.getCases(), command.getHearing()));

        final ArgumentCaptor<Hearing> hearingexArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        verify(this.hearingRepository).save(hearingexArgumentCaptor.capture());
        final Hearing actualHearing = hearingexArgumentCaptor.getValue();

        assertThat(actualHearing.getId(), is(hearing.getId()));
        assertThat(actualHearing.getCourtCentreId(), is(hearing.getCourtCentreId()));
        assertThat(actualHearing.getCourtCentreName(), is(hearing.getCourtCentreName()));
        assertThat(actualHearing.getRoomId(), is(hearing.getCourtRoomId()));
        assertThat(actualHearing.getRoomName(), is(hearing.getCourtRoomName()));
        assertThat(actualHearing.getStartDateTime().toLocalDateTime(), is(hearing.getStartDateTime().toLocalDateTime()));
        assertThat(actualHearing.getHearingType(), is(hearing.getType()));

        final Judge actualJudge = actualHearing.getAttendees().stream()
                .filter(a -> a instanceof Judge)
                .map(Judge.class::cast)
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
        assertThat(actualDefendant.getDefendantCases().get(0).getCustodyTimeLimitDate(), is(defendant.getDefendantCases().get(0).getCustodyTimeLimitDate()));

        final Address actualAddress = actualDefendant.getAddress();

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

        final ArgumentCaptor<LegalCase> caseArgumentCaptor = ArgumentCaptor.forClass(LegalCase.class);
        verify(this.legalCaseRepository).saveAndFlush(caseArgumentCaptor.capture());
        final LegalCase actualLegalCase = caseArgumentCaptor.getValue();

        assertThat(actualLegalCase.getId(), is(legalCase.getCaseId()));
        assertThat(actualLegalCase.getCaseUrn(), is(legalCase.getUrn()));
    }

    @Test
    public void convictionDateUpdated_shouldUpdateTheConvictionDate() throws Exception {

        final UUID caseId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = offenceId;
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        final ConvictionDateAdded convictionDateAdded = new ConvictionDateAdded(caseId, hearingId, offenceId, PAST_LOCAL_DATE.next());

        final Hearing hearing = Hearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(snapshotKey)
                                                .build()
                                ))
                                .build()))
                .build();

        final Offence offence = hearing.getDefendants().get(0).getOffences().get(0);

        when(this.offenceRepository.findBySnapshotKey(snapshotKey)).thenReturn(offence);

        initiateHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(convictionDateAdded.getOffenceId()));
        assertThat(offence.getConvictionDate(), is(convictionDateAdded.getConvictionDate()));
    }


    @Test
    public void convictionDateRemoved_shouldSetConvictionDateToNull() throws Exception {

        final UUID caseId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        final ConvictionDateRemoved convictionDateRemoved = new ConvictionDateRemoved(caseId, hearingId, offenceId);
        final Hearing hearing = Hearing.builder().withId(hearingId)
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
        
        final Offence offence = hearing.getDefendants().get(0).getOffences().get(0);
        
        when(offenceRepository.findBySnapshotKey(snapshotKey)).thenReturn(offence);

        initiateHearingEventListener.convictionDateRemoved(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
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
        
        final Hearing hearing = Hearing.builder().withId(hearingId)
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
        
        final Offence offence = hearing.getDefendants().get(0).getOffences().get(0);
        
        when(offenceRepository.findBySnapshotKey(snapshotKey)).thenReturn(offence);

        initiateHearingEventListener.hearingInitiatedPleaData(envelopeFrom(metadataWithRandomUUID("hearing.initiate-hearing-offence-plead"),
                objectToJsonObjectConverter.convert(event)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(offenceId));
        assertThat(offence.getId().getHearingId(), is(hearingId));
        assertThat(offence.getOriginHearingId(), is(originHearingId));
        assertThat(offence.getPleaDate(), is(pleaDate));
        assertThat(offence.getPleaValue(), is(pleaValue));
    }


    @Test
    public void shouldUpdateWitness() {
        final JsonEnvelope event = getWitnessAddedEnvelope();

        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(DEFENDANT_ID, HEARING_ID);
        final Hearing hearing = Hearing.builder().withId(HEARING_ID)
                .withDefendants(asList
                        (Defendant.builder()
                                .withId(snapshotKey)
                                .build()))

                .build();
        when(this.hearingRepository.findById(HEARING_ID)).thenReturn(hearing);

        this.initiateHearingEventListener.witnessAdded(event);
        final ArgumentCaptor<Hearing> hearingexArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        verify(this.hearingRepository).save(hearingexArgumentCaptor.capture());
        final Hearing actualHearing = hearingexArgumentCaptor.getValue();
        final Witness expectedWitnessOutcome = actualHearing.getDefendants().get(0).getDefendantWitnesses().get(0);
        assertThat(expectedWitnessOutcome.getId().getId(), is(TARGET_ID));
        assertThat(expectedWitnessOutcome.getHearing().getId(), is(HEARING_ID));
        assertThat(expectedWitnessOutcome.getType(), is(WITNESS_TYPE));
        assertThat(expectedWitnessOutcome.getClassification(), is(WITNESS_CLASSIFICATION));
        assertThat(expectedWitnessOutcome.getFirstName(), is(FIRSTNAME));
        assertThat(expectedWitnessOutcome.getLastName(), is(LASTNAME));
    }

    @Test
    public void shouldNotUpdateWitness_WhenHearingNotFound() {
        final JsonEnvelope event = getWitnessAddedEnvelope();
        when(this.hearingRepository.findById(HEARING_ID)).thenReturn(null);
        this.initiateHearingEventListener.witnessAdded(event);
        verify(hearingRepository, never()).save(any(Hearing.class));

    }

    @Test
    public void shouldEnricheWitnessWhenHearingFound() {
        final JsonEnvelope event = getWitnessEnrichEnvelope();

        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(DEFENDANT_ID, HEARING_ID);
        final Hearing hearing = Hearing.builder().withId(HEARING_ID)
                        .withDefendants(asList(Defendant.builder().withId(snapshotKey).build()))

                        .build();
        when(this.hearingRepository.findById(HEARING_ID)).thenReturn(hearing);
        when(this.witnessRepository.findBy(new HearingSnapshotKey(TARGET_ID, HEARING_ID)))
                        .thenReturn(null);
        this.initiateHearingEventListener.initiateHearingWitnessEnriched(event);
        final ArgumentCaptor<Hearing> hearingexArgumentCaptor =
                        ArgumentCaptor.forClass(Hearing.class);
        verify(this.hearingRepository).saveAndFlush(hearingexArgumentCaptor.capture());
        final Hearing actualHearing = hearingexArgumentCaptor.getValue();
        final Witness expectedWitnessOutcome =
                        actualHearing.getDefendants().get(0).getDefendantWitnesses().get(0);
        assertThat(expectedWitnessOutcome.getId().getId(), is(TARGET_ID));
        assertThat(expectedWitnessOutcome.getHearing().getId(), is(HEARING_ID));
        assertThat(expectedWitnessOutcome.getType(), is(WITNESS_TYPE));
        assertThat(expectedWitnessOutcome.getClassification(), is(WITNESS_CLASSIFICATION));
        assertThat(expectedWitnessOutcome.getFirstName(), is(FIRSTNAME));
        assertThat(expectedWitnessOutcome.getLastName(), is(LASTNAME));
    }

    @Test
    public void shouldNotEnrichWitness_WhenHearingNotFound() {
        final JsonEnvelope event = getWitnessEnrichEnvelope();
        when(this.hearingRepository.findById(HEARING_ID)).thenReturn(null);
        this.initiateHearingEventListener.initiateHearingWitnessEnriched(event);
        verify(hearingRepository, never()).save(any(Hearing.class));

    }
    public JsonEnvelope getWitnessAddedEnvelope() {
        final JsonObject witnessAdded = createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_PERSON_ID, PERSON_ID.toString())
                .add(FIELD_GENERIC_ID, TARGET_ID.toString())
                .add(FIELD_TYPE, WITNESS_TYPE)
                .add(FIELD_CLASSIFICATION, WITNESS_CLASSIFICATION)
                .add(FIELD_FIRST_NAME, FIRSTNAME)
                .add(FIELD_LAST_NAME, LASTNAME)
                .add(FIELD_DEFENDANT_IDS, createArrayBuilder().add(DEFENDANT_ID.toString()).build())
                .build();
        return envelopeFrom(metadataWithRandomUUIDAndName(), witnessAdded);
    }

    public JsonEnvelope getWitnessEnrichEnvelope() {
        final JsonObject witnessAdded = createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID.toString())
                        .add(FIELD_GENERIC_ID, TARGET_ID.toString()).add(FIELD_TYPE, WITNESS_TYPE)
                        .add(FIELD_CLASSIFICATION, WITNESS_CLASSIFICATION)
                        .add(FIELD_FIRST_NAME, FIRSTNAME).add(FIELD_LAST_NAME, LASTNAME)
                        .add(FIELD_DEFENDANT_ID, DEFENDANT_ID.toString()).build();
        return envelopeFrom(metadataWithRandomUUIDAndName(), witnessAdded);
    }
}
