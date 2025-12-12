package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedListElement;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FixedListDataLoaderTest {

    @Mock
    private Envelope<Object> allFixedlistEnvelope;
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

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void testLoadAllResultDefinitionData() {

        final LocalDate referenceDate = PAST_LOCAL_DATE.next();

        final AllFixedList allFixedList = AllFixedList.allFixedList().setFixedListCollection(
                asList(FixedList.fixedList()
                        .setId(UUID.randomUUID())
                        .setStartDate(LocalDate.now())
                        .setEndDate(LocalDate.now())
                        .setElements(asList(FixedListElement.fixedListElement()
                                .setCode("code")
                                .setValue("value")
                                .setWelshValue("welshCode")
                                .setCjsQualifier("cjsQualifiers")
                        ))

                )

        );

        when(requester.request(any(), any())).thenReturn(allFixedlistEnvelope);
        when(allFixedlistEnvelope.payload()).thenReturn(allFixedList);

        final AllFixedList actual = target.loadAllFixedList(envelopeFrom(metadataWithRandomUUID("something"), JsonValue.NULL), referenceDate);

        final FixedList fixedList = actual.getFixedListCollection().get(0);
        assertThat(fixedList, isBean(FixedList.class)
                .withValue(FixedList::getId, fixedList.getId())
                .withValue(FixedList::getStartDate, fixedList.getStartDate())
                .withValue(FixedList::getEndDate, fixedList.getEndDate())
                .with(FixedList::getElements, first(isBean(FixedListElement.class)
                        .withValue(FixedListElement::getCode, "code")
                        .withValue(FixedListElement::getValue, "value")
                        .withValue(FixedListElement::getWelshValue, "welshCode")
                        .withValue(FixedListElement::getCjsQualifier, "cjsQualifiers")
                ))
        );
    }

}
