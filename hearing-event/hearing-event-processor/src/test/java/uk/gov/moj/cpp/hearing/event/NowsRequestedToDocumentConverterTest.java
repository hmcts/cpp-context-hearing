package uk.gov.moj.cpp.hearing.event;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.nowdocument.NowDocumentRequest;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NowsRequestedToDocumentConverterTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @InjectMocks
    private NowsRequestedToDocumentConverter nowsRequestedToDocumentConverter;

    @Before
    public void setup() {

        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void convertNowsRequestToDocument() throws IOException {

        final CreateNowsRequest createNowsRequest = this.jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(getStringFromResource()), CreateNowsRequest.class);

        List<NowDocumentRequest> nowDocumentRequests = nowsRequestedToDocumentConverter.convert(null, createNowsRequest);

        assertThat(nowDocumentRequests.size(), is(2));
        assertThat(nowDocumentRequests.get(0).getNowContent().getAmendmentDate(), is("2019-06-24"));
    }

    private String getStringFromResource() throws IOException {
        return Resources.toString(getResource("createNowRequest.json"), defaultCharset());
    }
}