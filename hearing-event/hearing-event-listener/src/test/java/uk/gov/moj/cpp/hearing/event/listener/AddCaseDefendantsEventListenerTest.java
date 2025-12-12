package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.AddCaseDefendantsForHearing;
import uk.gov.moj.cpp.hearing.mapping.AssociatedDefenceOrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AssociatedPersonJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefendantJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PersonDefendantJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AddCaseDefendantsEventListenerTest {

    @Mock
    HearingRepository hearingRepository;
    @InjectMocks
    private AddCaseDefendantsEventListener addCaseDefendantsEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private DefendantJPAMapper defendantJPAMapper;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.defendantJPAMapper, "associatedPersonJPAMapper", new AssociatedPersonJPAMapper());
        setField(this.defendantJPAMapper, "organisationJPAMapper", new OrganisationJPAMapper());
        setField(this.defendantJPAMapper, "offenceJPAMapper", new OffenceJPAMapper());
        setField(this.defendantJPAMapper, "personDefendantJPAMapper", new PersonDefendantJPAMapper());
        setField(this.defendantJPAMapper, "associatedDefenceOrganisationJPAMapper", new AssociatedDefenceOrganisationJPAMapper());
    }

    @Test
    public void testCaseDefendantsUpdatedWhenNewDefendantsAdded() {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID caseId = randomUUID();

        final AddCaseDefendantsForHearing addCaseDefendantsForHearing =
                AddCaseDefendantsForHearing.addCaseDefendantsForHearing()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .withDefendants(
                                Arrays.asList(
                                        uk.gov.justice.core.courts.Defendant.defendant()
                                                .withId(defendantId)
                                                .build(),
                                        uk.gov.justice.core.courts.Defendant.defendant()
                                                .withId(defendantId1)
                                                .build()
                                        )
                        )
                        .build();
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final JsonEnvelope envelope = createJsonEnvelope(addCaseDefendantsForHearing);

        final Defendant defendant = new Defendant();

        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();

        prosecutionCase.setId(new HearingSnapshotKey(caseId, hearingId));

        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        addCaseDefendantsEventListener.addCaseDefendantsForHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());

        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        final ProsecutionCase prosecutionCaseOut = hearingOut.getProsecutionCases().stream().findFirst().get();

        final Set<Defendant> defendantsOut = prosecutionCaseOut.getDefendants();

        assertThat(2, is(defendantsOut.size()));
    }


    private JsonEnvelope createJsonEnvelope(final AddCaseDefendantsForHearing addCaseDefendantsForHearing) {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(addCaseDefendantsForHearing);
        final Metadata metadata = metadataOf(randomUUID(), "event-name").build();

        return envelopeFrom(metadata, jsonObject);
    }

}
