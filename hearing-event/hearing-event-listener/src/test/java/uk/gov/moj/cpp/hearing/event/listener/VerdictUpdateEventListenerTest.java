package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;

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
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
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

        final UUID caseId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final boolean unanimous = BOOLEAN.next();
        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setCaseId(caseId)
                .setHearingId(hearingId)
                .setOffenceId(offenceId)
                .setCategory(STRING.next())
                .setCategoryType(STRING.next())
                .setOffenceDefinitionId(randomUUID())
                .setOffenceCode(STRING.next())
                .setTitle(STRING.next())
                .setLegislation(STRING.next())
                .setNumberOfJurors(integer(9, 12).next())
                .setNumberOfSplitJurors(numberOfSplitJurors)
                .setUnanimous(unanimous)
                .setVerdictDate(PAST_LOCAL_DATE.next());

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(singletonList(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(singletonList(defendant));

        hearing.setProsecutionCases(singletonList(prosecutionCase));

        when(this.hearingRepository.findBy(hearingId)).thenReturn(hearing);

        verdictUpdateEventListener.verdictUpdate(envelopeFrom(metadataWithRandomUUID("hearing.offence-verdict-updated"),
                objectToJsonObjectConverter.convert(verdictUpsert)));

        verify(this.hearingRepository).save(hearing);

        final Offence result = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);

        assertThat(result.getId().getHearingId(), is(verdictUpsert.getHearingId()));
        assertThat(result.getId().getId(), is(verdictUpsert.getOffenceId()));
        assertThat(result.getVerdict().getVerdictType().getVerdictCategory(), is(verdictUpsert.getCategory()));
        assertThat(result.getVerdict().getVerdictType().getVerdictCategoryType(), is(verdictUpsert.getCategoryType()));
        assertThat(result.getVerdict().getVerdictType().getVerdictTypeId(), is(verdictUpsert.getVerdictTypeId()));
        assertThat(result.getVerdict().getLesserOrAlternativeOffence().getLesserOffenceTitle(), is(verdictUpsert.getTitle()));
        assertThat(result.getVerdict().getLesserOrAlternativeOffence().getLesserOffenceCode(), is(verdictUpsert.getOffenceCode()));
        assertThat(result.getVerdict().getLesserOrAlternativeOffence().getLesserOffenceLegislation(), is(verdictUpsert.getLegislation()));
        assertThat(result.getVerdict().getJurors().getNumberOfJurors(), is(verdictUpsert.getNumberOfJurors()));
        assertThat(result.getVerdict().getJurors().getNumberOfSplitJurors(), is(verdictUpsert.getNumberOfSplitJurors()));
        assertThat(result.getVerdict().getJurors().getUnanimous(), is(verdictUpsert.getUnanimous()));
        assertThat(result.getVerdict().getVerdictDate(), is(verdictUpsert.getVerdictDate()));
    }
}