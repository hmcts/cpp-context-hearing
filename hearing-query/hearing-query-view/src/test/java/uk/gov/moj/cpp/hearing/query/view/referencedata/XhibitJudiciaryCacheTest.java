package uk.gov.moj.cpp.hearing.query.view.referencedata;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;
import uk.gov.moj.cpp.listing.domain.referencedata.Judiciary;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitJudiciaryCacheTest {

    @Mock
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @InjectMocks
    private XhibitJudiciaryCache xhibitJudiciaryCache;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setUp() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldGetJudiciaryName() {

        final UUID judiciaryId = randomUUID();
        final String titleSuffix = "judge";
        final String titlePrefix = "Mr";
        final String titleJudicialPrefix = "Recorder";
        final String surname = "Ainsworth";
        final String forenames = "Mark J";

        final Judiciary judiciary = new Judiciary.Builder()
                .withId(judiciaryId)
                .withTitlePrefix(titlePrefix)
                .withTitleJudicialPrefix(titleJudicialPrefix)
                .withTitleSuffix(titleSuffix)
                .withSurname(surname)
                .withForenames(forenames)
                .build();

        final JsonObject judiciaryJsonObject = objectToJsonObjectConverter.convert(judiciary);
        when(commonXhibitReferenceDataService.getJudiciary(judiciaryId)).thenReturn(judiciaryJsonObject);

        final String fullName = xhibitJudiciaryCache.getJudiciaryName(judiciaryId);

        assertThat(fullName, is("Recorder Mark J Ainsworth judge"));
        verify(commonXhibitReferenceDataService, times(1)).getJudiciary(judiciaryId);
        verifyNoMoreInteractions(commonXhibitReferenceDataService);
    }
}
