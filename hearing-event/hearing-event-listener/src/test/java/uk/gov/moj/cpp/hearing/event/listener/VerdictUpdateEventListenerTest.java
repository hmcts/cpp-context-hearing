package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;
import uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class VerdictUpdateEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private VerdictUpdateEventListener verdictUpdateEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
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
                .setVerdict(uk.gov.justice.json.schemas.core.Verdict.verdict()
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withOffenceId(offenceId)
                        .withOriginatingHearingId(randomUUID())
                        .withJurors(
                                uk.gov.justice.json.schemas.core.Jurors.jurors()
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(numberOfSplitJurors)
                                        .withUnanimous(unanimous)
                                        .build())
                        .withVerdictType(
                                uk.gov.justice.json.schemas.core.VerdictType.verdictType()
                                        .withVerdictTypeId(randomUUID())
                                        .withCategoryType(STRING.next())
                                        .withCategory(STRING.next())
                                        .withDescription(STRING.next())
                                        .withSequence(INTEGER.next())
                                        .build())
                        .withLesserOrAlternativeOffence(uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
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

        verdictUpdateEventListener.verdictUpdate(envelopeFrom(metadataWithRandomUUID("hearing.offence-verdict-updated"),
                objectToJsonObjectConverter.convert(verdictUpsert)));

        verify(this.hearingRepository).save(hearing);

        final Offence result = hearing.getProsecutionCases().iterator().next().getDefendants().iterator().next().getOffences().iterator().next();

        assertThat(result.getId().getHearingId(), is(verdictUpsert.getHearingId()));

        final uk.gov.justice.json.schemas.core.Verdict verdictPojo = verdictUpsert.getVerdict();

        assertThat(result.getVerdict(), isBean(Verdict.class)
                .with(Verdict::getOriginatingHearingId, is(verdictPojo.getOriginatingHearingId()))
                .with(Verdict::getVerdictDate, is(verdictPojo.getVerdictDate()))
                .with(Verdict::getVerdictType, isBean(VerdictType.class)
                        .with(VerdictType::getVerdictTypeId, is(verdictPojo.getVerdictType().getVerdictTypeId()))
                        .with(VerdictType::getVerdictCategory, is(verdictPojo.getVerdictType().getCategory()))
                        .with(VerdictType::getVerdictCategoryType, is(verdictPojo.getVerdictType().getCategoryType()))
                        .with(VerdictType::getDescription, is(verdictPojo.getVerdictType().getDescription()))
                        .with(VerdictType::getSequence, is(verdictPojo.getVerdictType().getSequence())))
                .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                        .with(LesserOrAlternativeOffence::getLesserOffenceCode, is(verdictPojo.getLesserOrAlternativeOffence().getOffenceCode()))
                        .with(LesserOrAlternativeOffence::getLesserOffenceTitle, is(verdictPojo.getLesserOrAlternativeOffence().getOffenceTitle()))
                        .with(LesserOrAlternativeOffence::getLesserOffenceLegislation, is(verdictPojo.getLesserOrAlternativeOffence().getOffenceLegislation()))
                        .with(LesserOrAlternativeOffence::getLesserOffenceDefinitionId, is(verdictPojo.getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                        .with(LesserOrAlternativeOffence::getLesserOffenceTitleWelsh, is(verdictPojo.getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                        .with(LesserOrAlternativeOffence::getLesserOffenceLegislationWelsh, is(verdictPojo.getLesserOrAlternativeOffence().getOffenceLegislationWelsh())))
                .with(Verdict::getJurors, isBean(Jurors.class)
                        .with(Jurors::getNumberOfJurors, is(verdictPojo.getJurors().getNumberOfJurors()))
                        .with(Jurors::getNumberOfSplitJurors, is(verdictPojo.getJurors().getNumberOfSplitJurors()))
                        .with(Jurors::getUnanimous, is(verdictPojo.getJurors().getUnanimous()))));
    }
}