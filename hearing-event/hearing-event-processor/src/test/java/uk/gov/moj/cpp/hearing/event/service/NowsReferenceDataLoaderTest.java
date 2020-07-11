package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceDataLoaderTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingInitiated.class
    );
    @InjectMocks
    NowsReferenceDataLoader target;
    @Mock
    private Requester requester;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void testLoadAllResultDefinitionData() {

        LocalDate referenceDate = PAST_LOCAL_DATE.next();

        AllResultDefinitions allResultDefinitionsIn = AllResultDefinitions.allResultDefinitions().setResultDefinitions(
                asList(ResultDefinition.resultDefinition()
                        .setId(UUID.randomUUID()).setWelshLabel("Idris")
                        .setIsAvailableForCourtExtract(true)
                        .setUserGroups(asList("   Court Admin "))
                        .setPrompts(asList(Prompt.prompt()
                                .setUserGroups(asList("   Court Admin "))
                        ))

                )

        );

        final JsonEnvelope resultEnvelope = envelopeFrom(metadataWithRandomUUID("something"), objectToJsonObjectConverter.convert(allResultDefinitionsIn));

        when(requester.requestAsAdmin(any())).thenReturn(resultEnvelope);

        AllResultDefinitions actual = target.getAllResultDefinition(envelopeFrom(metadataWithRandomUUID("something"), JsonValue.NULL), referenceDate);

        assertThat(actual.getResultDefinitions().get(0), isBean(ResultDefinition.class)
                .withValue(ResultDefinition::getId, actual.getResultDefinitions().get(0).getId())
                .withValue(ResultDefinition::getWelshLabel, actual.getResultDefinitions().get(0).getWelshLabel())
                .withValue(ResultDefinition::getIsAvailableForCourtExtract, actual.getResultDefinitions().get(0).getIsAvailableForCourtExtract())
                .withValue(rd -> rd.getUserGroups().get(0), "Court Admin")
                .with(ResultDefinition::getPrompts, first(isBean(Prompt.class)
                        .withValue(p -> p.getUserGroups().get(0), "Court Admin")
                ))
        );
    }
}
