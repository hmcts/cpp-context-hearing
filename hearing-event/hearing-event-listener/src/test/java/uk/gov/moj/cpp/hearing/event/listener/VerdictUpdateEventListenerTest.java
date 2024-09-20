package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VerdictUpdateEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private VerdictJPAMapper verdictJPAMapper;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @InjectMocks
    private VerdictUpdateEventListener verdictUpdateEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void verdictUpdate_shouldUpdateTheVerdict() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final boolean unanimous = BOOLEAN.next();
        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingId)
                .setVerdict(uk.gov.justice.core.courts.Verdict.verdict()
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withOffenceId(offenceId)
                        .withOriginatingHearingId(randomUUID())
                        .withJurors(
                                uk.gov.justice.core.courts.Jurors.jurors()
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(numberOfSplitJurors)
                                        .withUnanimous(unanimous)
                                        .build())
                        .withVerdictType(
                                uk.gov.justice.core.courts.VerdictType.verdictType()
                                        .withId(randomUUID())
                                        .withCategoryType(STRING.next())
                                        .withCategory(STRING.next())
                                        .withDescription(STRING.next())
                                        .withSequence(INTEGER.next())
                                        .build())
                        .withLesserOrAlternativeOffence(uk.gov.justice.core.courts.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                .withOffenceLegislationWelsh(STRING.next())
                                .withOffenceLegislation(STRING.next())
                                .withOffenceTitleWelsh(STRING.next())
                                .withOffenceTitle(STRING.next())
                                .withOffenceCode(STRING.next())
                                .withOffenceDefinitionId(randomUUID())
                                .build())
                        .build());

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(asSet(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        when(this.hearingRepository.findBy(hearingId)).thenReturn(hearing);

        final Verdict verdict = new Verdict();

        when(verdictJPAMapper.toJPA(Mockito.any())).thenReturn(verdict);

        verdictUpdateEventListener.verdictUpdate(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-verdict-updated"),
                objectToJsonObjectConverter.convert(verdictUpsert)));

        verify(this.hearingRepository).save(hearing);

    }

    @Test
    public void shouldUpdateCourtApplicationWithVerdict(){
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();
        final boolean unanimous = BOOLEAN.next();
        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        uk.gov.justice.core.courts.Verdict applicationVerdict = uk.gov.justice.core.courts.Verdict.verdict()
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withApplicationId(applicationId)
                .withOriginatingHearingId(randomUUID())
                .withJurors(
                        uk.gov.justice.core.courts.Jurors.jurors()
                                .withNumberOfJurors(integer(9, 12).next())
                                .withNumberOfSplitJurors(numberOfSplitJurors)
                                .withUnanimous(unanimous)
                                .build())
                .withVerdictType(
                        uk.gov.justice.core.courts.VerdictType.verdictType()
                                .withId(randomUUID())
                                .withCategoryType(STRING.next())
                                .withCategory(STRING.next())
                                .withDescription(STRING.next())
                                .withSequence(INTEGER.next())
                                .build())
                .withLesserOrAlternativeOffence(uk.gov.justice.core.courts.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                        .withOffenceLegislationWelsh(STRING.next())
                        .withOffenceLegislation(STRING.next())
                        .withOffenceTitleWelsh(STRING.next())
                        .withOffenceTitle(STRING.next())
                        .withOffenceCode(STRING.next())
                        .withOffenceDefinitionId(randomUUID())
                        .build())
                .build();

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingId)
                .setVerdict(applicationVerdict);

        final Hearing hearingEntity = new Hearing();
        hearingEntity.setId(hearingId);
        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withId(applicationId)
                .withVerdict(applicationVerdict).build();
        final String str = objectToJsonObjectConverter.convert(courtApplication).toString();

        final uk.gov.justice.core.courts.Hearing hearing = uk.gov.justice.core.courts.Hearing.hearing()
                .withCourtApplications(Collections.singletonList(CourtApplication.courtApplication().withId(applicationId).build()))
                .build();

        when(this.hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);
        when(this.hearingJPAMapper.fromJPA(hearingEntity)).thenReturn(hearing);
        when(hearingJPAMapper.addOrUpdateCourtApplication(any(),any() )).thenReturn("["+str+"]");

        verdictUpdateEventListener.verdictUpdate(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-verdict-updated"),
                objectToJsonObjectConverter.convert(verdictUpsert)));

        final ArgumentCaptor<CourtApplication> courtApplicationArgumentCaptor = ArgumentCaptor.forClass(CourtApplication.class);
        verify(hearingJPAMapper).addOrUpdateCourtApplication(any(), courtApplicationArgumentCaptor.capture());
        final CourtApplication updatedCourtApplication = courtApplicationArgumentCaptor.getValue();
        assertThat(updatedCourtApplication, is(courtApplication));

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        verify(hearingRepository).save(hearingArgumentCaptor.capture());
        final Hearing savedHearing = hearingArgumentCaptor.getValue();
        assertThat(savedHearing.getCourtApplicationsJson(), is("[{\"id\":\"" + applicationId.toString() + "\",\"verdict\":"+objectToJsonObjectConverter.convert(applicationVerdict)+"}]"));

    }

    @Test
    public void verdictUpdate_shouldUpdateTheVerdictToOffenceInCourtApplication() {

        final UUID applicationId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final LocalDate verdictDate = LocalDate.now();
        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingId)
                .setVerdict(uk.gov.justice.core.courts.Verdict.verdict().withVerdictDate(verdictDate).build());

        final Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("abc");


        when(this.hearingRepository.findBy(verdictUpsert.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.updateVerdictOnOffencesInCourtApplication(eq(hearing.getCourtApplicationsJson()), any(uk.gov.justice.core.courts.Verdict.class))).thenReturn("def");

        verdictUpdateEventListener.verdictUpdate(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-verdict-updated"),
                objectToJsonObjectConverter.convert(verdictUpsert)));

        verify(this.hearingRepository).save(hearing);
        assertThat(hearing.getCourtApplicationsJson(), is("def"));

    }
}